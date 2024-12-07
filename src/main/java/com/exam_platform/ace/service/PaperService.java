package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaperService {

	private final PaperRepository paperRepository;

	public Paper getPaperById(Paper.Id id) {
		return paperRepository.findById(id).orElse(null);
	}
}
