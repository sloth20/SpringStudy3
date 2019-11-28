package com.sp.notice;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.common.FileManager;
import com.sp.common.MyUtil;
import com.sp.member.SessionInfo;

@Controller("notice.noticeController")
public class NoticeController {

	@Autowired
	private NoticeService service;

	@Autowired
	private MyUtil myUtil;

	@Autowired
	private FileManager fileManager;

	@RequestMapping(value = "/notice/list")
	public String list(@RequestParam(name = "page", defaultValue = "1") int current_page,
			@RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "") String keyword,
			HttpServletRequest req, Model model) throws Exception {

		String cp = req.getContextPath();

		int rows = 10;
		int total_page = 0;
		int dataCount = 0;

		if (req.getMethod().equalsIgnoreCase("GET")) {
			// GET 방식인 경우
			keyword = URLDecoder.decode(keyword, "utf-8");
		}

		// 전체 페이지 수
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("condition", condition);
		map.put("keyword", keyword);

		dataCount = service.dataCount(map);
		if (dataCount != 0)
			total_page = myUtil.pageCount(rows, dataCount);

		// 다른 사람이 자료를 삭제하여 전체 페이지수가 변화 된 경우
		if (total_page < current_page)
			current_page = total_page;

		// 1페이지인 경우 공지 리스트 가져오기
		List<Notice> noticeList = null;
		if (current_page == 1) {
			noticeList = service.listNoticeTop();
		}

		// 리스트에 출력할 데이터를 가져오기
		int offset = (current_page - 1) * rows;
		if (offset < 0)
			offset = 0;
		map.put("offset", offset);
		map.put("rows", rows);

		List<Notice> list = service.listNotice(map);

		// 리스트의 번호

		Date endDate = new Date();
		long gap;

		int listNum, n = 0;
		for (Notice dto : list) {
			listNum = dataCount - (offset + n);
			dto.setListNum(listNum);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date beginDate = formatter.parse(dto.getCreated());

			// 날짜 차이(일)
//			gap = (endDate.getTime() - beginDate.getTime())/(24*60*60*1000);
//			dto.setGap(gap);

			// 날짜 차이(시간)
			gap = (endDate.getTime() - beginDate.getTime()) / (60 * 60 * 1000);
			dto.setGap(gap);

			dto.setCreated(dto.getCreated().substring(0, 10));

			n++;
		}

		String query = "";
		String listUrl = cp + "/notice/list";
		String articleUrl = cp + "/notice/article?page=" + current_page;
		if (keyword.length() != 0) {
			query = "condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "utf-8");
		}

		if (query.length() != 0) {
			listUrl = cp + "/notice/list?" + query;
			articleUrl = cp + "/notice/article?page=" + current_page + "&" + query;
		}

		String paging = myUtil.paging(current_page, total_page, listUrl);

		model.addAttribute("noticeList", noticeList);
		model.addAttribute("list", list);
		model.addAttribute("articleUrl", articleUrl);
		model.addAttribute("page", current_page);
		model.addAttribute("total_page", total_page);
		model.addAttribute("dataCount", dataCount);
		model.addAttribute("paging", paging);

		model.addAttribute("condition", condition);
		model.addAttribute("keyword", keyword);

		return ".notice.list";
	}

	@RequestMapping(value = "/notice/created")
	public String createdForm(HttpSession session, Model model) throws Exception {

		SessionInfo info = (SessionInfo) session.getAttribute("member");
		if (!info.getUserId().equals("admin")) {
			return "redirect:/notice/list";
		}

		model.addAttribute("mode", "created");
		return ".notice.created";
	}

	@RequestMapping(value = "/notice/created", method = RequestMethod.POST)
	public String createdSubmit(HttpSession session, Notice dto) throws Exception {
		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		SessionInfo info = (SessionInfo) session.getAttribute("member");
		dto.setUserId(info.getUserId());

		try {
			service.insertNotice(dto, pathname);
		} catch (Exception e) {
		}
		return "redirect:/notice/list";
	}

	@RequestMapping(value = "/notice/zipdownload")
	public void zip(@RequestParam int num, HttpServletResponse resp, HttpSession session) throws Exception {
		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		boolean b = false;
		List<Notice> list = service.listFile(num);

		if (list.size() > 0) {
			String[] sources = new String[list.size()];
			String[] originals = new String[list.size()];
			String zipFilename = num + ".zip";

			for (int idx = 0; idx < list.size(); idx++) {
				sources[idx] = pathname + File.separator + list.get(idx).getSaveFilename();
				originals[idx] = File.separator + list.get(idx).getOriginalFilename();
			}

			b = fileManager.doZipFileDownload(sources, originals, zipFilename, resp);
		}

		if (!b) {
			resp.setContentType("text/html;charset=utf-8");
			PrintWriter out = resp.getWriter();
			out.print("<script>alert('다운로드 불가...');history.back();</script>");
		}
	}

	@RequestMapping(value = "/notice/article")
	public String article(@RequestParam int num, @RequestParam String page,
			@RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "") String keyword,
			Model model) throws Exception {

		keyword = URLDecoder.decode(keyword, "UTF-8");

		String query = "page=" + page;
		if (keyword.length() != 0) {
			query += "&condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
		}

		service.updateHitCount(num);

		Notice dto = service.readNotice(num);
		if (dto == null)
			return "redirect:/notice/list?" + query;

		// 스타일로 처리하는 경우 : style="white-space:pre;"

		dto.setContent(dto.getContent().replaceAll("\n", "<br>"));

		Map<String, Object> map = new HashMap<>();

		map.put("condition", condition);
		map.put("keyword", keyword);
		map.put("num", num);

		Notice preReadDto = service.preReadNotice(map);
		Notice nextReadDto = service.nextReadNotice(map);

		// 파일
		List<Notice> listFile = service.listFile(num);

		model.addAttribute("dto", dto);
		model.addAttribute("preReadDto", preReadDto);
		model.addAttribute("nextReadDto", nextReadDto);
		model.addAttribute("listFile", listFile);

		model.addAttribute("page", page);
		model.addAttribute("query", query);

		return ".notice.article";
	}

	@RequestMapping(value = "/notice/update", method = RequestMethod.GET)
	public String updateForm(@RequestParam int num, @RequestParam String page, HttpSession session, Model model) {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		Notice dto = service.readNotice(num);
		if (dto == null || !dto.getUserId().equals(info.getUserId())) {
			return "redirect:/notice/list?page=" + page;
		}

		List<Notice> listFile = service.listFile(num);

		model.addAttribute("mode", "update");
		model.addAttribute("dto", dto);
		model.addAttribute("listFile", listFile);
		model.addAttribute("page", page);

		return ".notice.created";
	}

	@RequestMapping(value = "/notice/update", method = RequestMethod.POST)
	public String updateSubmit(Notice dto, @RequestParam String page, HttpSession session) {

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		try {
			service.updateNotice(dto, pathname);
		} catch (Exception e) {
		}

		return "redirect:/notice/list?page=" + page;
	}

	@RequestMapping(value = "/notice/download")
	public void download(@RequestParam int fileNum, HttpServletResponse resp, HttpSession session) throws IOException {
		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		Notice dto = service.readFile(fileNum);
		boolean b = false;

		if (dto != null) {
			b = fileManager.doFileDownload(dto.getSaveFilename(), dto.getOriginalFilename(), pathname, resp);
		}

		if (!b) {
			resp.setContentType("text/html;charset=utf-8");
			PrintWriter out = resp.getWriter();
			out.print("<script>alert('파일 다운로드가 실패 했습니다.');history.back();</script>");
		}

	}

	@RequestMapping(value = "/notice/deleteFile", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> deleteFile(@RequestParam int fileNum, HttpSession session) throws Exception {
		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		String state = "false";

		Notice dto = service.readFile(fileNum);
		if (dto != null) {
			fileManager.doFileDelete(dto.getSaveFilename(), pathname);

			Map<String, Object> map = new HashMap<>();
			map.put("field", "fileNum");
			map.put("num", fileNum);

			try {
				service.deleteFile(map);
				state = "true";
			} catch (Exception e) {
			}
		}

		Map<String, Object> model = new HashMap<>();
		model.put("state", state);
		return model;
	}

	@RequestMapping(value = "/notice/delete", method = RequestMethod.GET)
	public String delete(@RequestParam int num, @RequestParam String page,
			@RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "") String keyword,
			HttpSession session, Model model) throws Exception {

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		keyword = URLDecoder.decode(keyword, "UTF-8");

		String query = "page=" + page;
		if (keyword.length() != 0) {
			query += "&condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
		}

		SessionInfo info = (SessionInfo) session.getAttribute("member");
		Notice dto = service.readNotice(num);
		if (dto != null && (dto.getUserId().equals(info.getUserId()) || info.getUserId().equals("admin"))) {
			service.deleteNotice(num, pathname);
		}

		return "redirect:/notice/list?" + query;
	}

}
