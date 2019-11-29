package com.sp.guest;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.common.MyUtil;
import com.sp.member.SessionInfo;

@Controller("guest.guestController2")
public class GuestController2 {

	@Autowired
	private GuestService service;

	@Autowired
	private MyUtil myUtil;

	@RequestMapping("/guest/guest2")
	public String main() throws Exception {
		return ".guest2.guest";
	}

	// AJAX : XML(@ResponseBody 이용)
	@RequestMapping(value = "/guest/insert2", method = RequestMethod.POST)
	@ResponseBody
	public StringData submit(Guest dto, HttpSession session) throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");
		dto.setUserId(info.getUserId());

		String state = "true";
		try {
			service.insertGuest(dto);
		} catch (Exception e) {
			state = "false";
		}

		return new StringData(state);

	}

	// AJAX : XML - @ResponseBody를 사용하지 않는 경우
	@RequestMapping(value = "/guest/list2")
	public String list(@RequestParam(value = "pageNo", defaultValue = "1") int current_page, Model model)
			throws Exception {

		int rows = 5;
		int offset;
		int total_page;
		int dataCount;

		dataCount = service.dataCount();
		total_page = myUtil.pageCount(rows, dataCount);

		offset = (current_page - 1) * rows;
		if (offset < 0)
			offset = 0;

		Map<String, Object> map = new HashMap<>();
		map.put("offset", offset);
		map.put("rows", rows);

		List<Guest> list = service.listGuest(map);
		for (Guest dto : list) {
			dto.setContent(myUtil.htmlSymbols(dto.getContent()));
		}

		String paging = myUtil.pagingMethod(current_page, total_page, "listPage");

		model.addAttribute("list", list);
		model.addAttribute("dataCount", dataCount);
		model.addAttribute("pageNo", current_page);
		model.addAttribute("total_page", total_page);
		model.addAttribute("paging", paging);

		return "guest2/list"; // jsp로 포워딩(XML을 클라이언트에게 전달)
	}

	// AJAX : XML - @ResponseBody를 사용하지 않는 경우
	@RequestMapping(value = "/guest/delete2", method = RequestMethod.POST)
	public void delete(@RequestParam Map<String, Object> paramMap, HttpServletResponse resp, HttpSession session)
			throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		String state = "true";

		try {
			paramMap.put("userId", info.getUserId());
			service.deleteGuest(paramMap);

		} catch (Exception e) {
			state = "false";
		}

		// XML을 클라이언트에게 전송
		StringBuilder sb = new StringBuilder();
		sb.append("<root><state");
		sb.append(state);
		sb.append("</state></root>");
	
		resp.setContentType("text/xml;charset=utf-8");
		PrintWriter out = resp.getWriter();
		out.print(sb.toString());
	
	
	}
	
}
