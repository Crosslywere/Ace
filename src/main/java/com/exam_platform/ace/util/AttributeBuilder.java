package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.model.PageRoute;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AttributeBuilder {

	public static Map<String, Object> build(String pageTitle, PageRoute route) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("pageTitle", pageTitle);
		map.put("currentRoute", route);
		return map;
	}

	public static Map<String, Object> buildForTable(String pageTitle, PageRoute route, List<Exam> exams, int maxPages, int currentPage) {
		var map = build(pageTitle, route);
		map.put("exams", exams);
		map.put("maxPages", maxPages);
		map.put("currentPage", currentPage);
		return map;
	}

	public static Map<String, Object> buildForTable(String pageTitle, PageRoute route, Exam exam, List<Candidate> candidates, int maxPages, int currentPage) {
		var map = build(pageTitle, route);
		map.put("exam", exam);
		map.put("maxPages", maxPages);
		map.put("candidates", candidates);
		map.put("currentPage", currentPage);
		return map;
	}

	public static Map<String, Object> buildForForm(String pageTitle, PageRoute route, Exam exam) {
		var map = build(pageTitle, route);
		map.put("exam", exam);
		map.put("minDate", Date.valueOf(LocalDate.now()));
		return map;
	}
}
