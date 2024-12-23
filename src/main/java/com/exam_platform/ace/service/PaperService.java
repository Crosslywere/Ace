package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.repository.PaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaperService {

	private final PaperRepository paperRepository;

	public PaperService(@Autowired PaperRepository paperRepository) {
		this.paperRepository = paperRepository;
	}

	public Paper getPaperById(Paper.Id id) {
		return paperRepository.findById(id).orElse(null);
	}
}
