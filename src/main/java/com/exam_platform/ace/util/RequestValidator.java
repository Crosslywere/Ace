package com.exam_platform.ace.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestValidator {

	public static boolean isLocalhost(HttpServletRequest request) {
		System.out.print("Request URL size = " + request.getRequestURL().length() + " Request URL = " + request.getRequestURL().toString());
		return true;
	}
}
