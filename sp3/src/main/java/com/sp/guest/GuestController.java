package com.sp.guest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.common.MyUtil;
import com.sp.member.SessionInfo;

@Controller("guest.guestController")
public class GuestController {

	@Autowired
	private GuestService service;

	@Autowired
	private MyUtil myUtil;

	@RequestMapping("/guest/guest")
	public String main() throws Exception {
		return ".guest.guest";
	}

	@RequestMapping(value = "/guest/insert", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> submit(Guest dto, HttpSession session) throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");
		dto.setUserId(info.getUserId());

		String state = "true";
		try {
			service.insertGuest(dto);
		} catch (Exception e) {
			state = "false";
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("state", state);
		return model;

	}

	@RequestMapping(value = "/guest/list")
	@ResponseBody
	public Map<String, Object> list(@RequestParam(value = "pageNo", defaultValue = "1") int current_page)
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
		
		
//		try {
//			Thread.sleep(3000);
//		} catch (Exception e) {
//		}
		
		
		
		
		
		
		Map<String, Object> model = new HashMap<>();
		// model에 list, dataCount, pageNo, total_page, paging을 put
		model.put("list", list);
		model.put("dataCount", dataCount);
		model.put("pageNo", current_page);
		model.put("total_page", total_page);
		model.put("paging", paging);

		return model;
	}

	@RequestMapping(value = "/guest/delete", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> delete(@RequestParam Map<String, Object> paramMap, HttpSession session)
			throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		
		try {
			paramMap.put("userId", info.getUserId());
			service.deleteGuest(paramMap);

		} catch (Exception e) {
		}
		
		String page = (String)paramMap.get("pageNo");

		return list(Integer.parseInt(page));
	}
}
