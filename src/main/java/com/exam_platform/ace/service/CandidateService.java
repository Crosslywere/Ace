package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateService {

	private final CandidateRepository candidateRepository;

	public List<Candidate> getCandidatesByExam(Exam exam, Pageable pageable) {
		return candidateRepository.findByExam(exam, pageable);
	}


	public List<Candidate> getCandidatesByUsername() {
		return List.of();
	}
}
