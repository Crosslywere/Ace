package com.exam_platform.ace.model;

public enum PageRoute {
	SCHEDULED("scheduled"),
	ONGOING("ongoing"),
	RECORDED("recorded"),
	CREATE("create"),
	SEARCH("search");

	private final String page;

	PageRoute(String page) {
		this.page = page;
	}

	public String getPage() {
		return page;
	}
}
