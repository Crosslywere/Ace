package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperService {

	private final PaperRepository paperRepository;

	public void deleteAll(List<Paper> papers) {
		paperRepository.deleteAll(papers);
	}
}
