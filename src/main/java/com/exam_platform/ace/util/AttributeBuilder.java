package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.model.PageRoute;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attributes to consider<p/>
 * <ol>
 *     <li class="text-center">pageTitle - sets the title of the page</li>
 *     <li>currentRoute - an enum with an attribute {@code page} used for pagination.</li>
 *     <li></li>
 * </ol>
 */
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
}
