package com.exam_platform.ace.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestValidator {

	public static boolean isLocalhost(HttpServletRequest request) {
		String url = request.getRequestURL().substring(7);
		return url.startsWith("localhost") || url.startsWith("127.0.0.1");
	}
}
