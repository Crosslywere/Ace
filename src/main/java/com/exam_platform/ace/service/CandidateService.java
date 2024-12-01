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

	public List<Candidate> getCandidatesByExamAndUsername(Exam exam, String username) {
		return candidateRepository.findByExamAndId_UsernameContainingIgnoreCase(exam, username);
	}

	public void resetCandidateById(Candidate.Id candidateId) {
		var candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate == null || candidate.getExam().getState() == Exam.State.RECORDED) {
			return;
		}
		candidate.getAnswers().clear();
		candidate.setLoggedIn(false);
		candidate.setSubmitted(false);
		candidateRepository.save(candidate);
	}

	public Candidate loginCandidate(Candidate.Id candidateId, String password) {
		var candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate == null || candidate.getExam().getState() != Exam.State.ONGOING) {
			return null;
		}
		if (candidate.getExam().isPasswordRequired() && !candidate.getPassword().equals(password)) {
			return null;
		}
		if (candidate.isLoggedIn()) {
			return null;
		}
		candidate.setLoggedIn(true);
		return candidateRepository.save(candidate);
	}

	public List<Candidate> getCandidatesByExamId(Long examId) {
		return candidateRepository.findById_ExamId(examId);
	}
}
