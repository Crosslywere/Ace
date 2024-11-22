package com.exam_platform.ace.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PageRoute {
	SCHEDULED("scheduled"),
	ONGOING("ongoing"),
	RECORDED("recorded"),
	CREATE("create"),
	SEARCH("search");

	private final String page;
}
