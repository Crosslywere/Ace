package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.CandidateAnswer;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.entity.Paper;
import com.exam_platform.ace.repository.CandidateRepository;
import com.exam_platform.ace.repository.ExamRepository;
import com.exam_platform.ace.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CandidateService {

	private final CandidateRepository candidateRepository;

	private final ExamRepository examRepository;

	private final PaperRepository paperRepository;

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
		candidate.setTimeIn(null);
		candidate.setTimeUsed(0.0f);
		candidate.setLoggedIn(false);
		candidate.setSubmitted(false);
		candidate.getAnswers().clear();
		candidateRepository.save(candidate);
	}

	public Candidate loginCandidate(Candidate.Id candidateId, String password) {
		var candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate == null ||
				candidate.getExam().getState() != Exam.State.ONGOING ||
				(candidate.getExam().isPasswordRequired() && !candidate.getPassword().equals(password)) ||
				candidate.isLoggedIn() || candidate.isSubmitted()) {
			return null;
		}
		candidate.setLoggedIn(true);
		candidate.setTimeIn(Time.valueOf(LocalTime.now()));
		setCandidateQuestions(candidate);
		return candidateRepository.save(candidate);
	}

	public void logoutCandidateById(Candidate.Id candidateId) {
		var candidate = candidateRepository.findById(candidateId).orElse(null);
		if (candidate == null) {
			return;
		}
		candidate.setLoggedIn(false);
		candidateRepository.save(candidate);
	}

	public void logoutCandidatesByExamId(Long examId) {
		var candidates = candidateRepository.findById_ExamId(examId);
		candidates.stream().filter(candidate -> !candidate.isSubmitted()).forEach(candidate -> {
			candidate.setLoggedIn(false);
			candidateRepository.save(candidate);
		});
	}

	public List<Candidate> getCandidatesByExamId(Long examId) {
		return candidateRepository.findById_ExamId(examId);
	}

	public Candidate getCandidateById(Candidate.Id candidateId) {
		return candidateRepository.findById(candidateId).orElse(null);
	}

	private void setCandidateQuestions(Candidate candidate) {
		Long examId = candidate.getId().getExamId();
		Exam exam = examRepository.findById(examId).orElse(null);
		if (exam == null) {
			return;
		}
		// Enforce Mandatory Papers
		List<Paper> mandatoryPapers = exam.getPapers().stream().filter(Paper::isMandatory).toList();
		for (var paper : mandatoryPapers) {
			if (!candidate.getPapers().contains(paper.getId().getName())) {
				candidate.getPapers().add(paper.getId().getName());
			}
		}
		// Add Paper's Shuffled Questions
		for (var paperName : candidate.getPapers()) {
			Paper paper = paperRepository.findById(Paper.Id.builder().examId(examId).name(paperName).build()).orElse(null);
			if (paper == null) {
				candidate.getPapers().remove(paperName);
				continue;
			}
			appendQuestions(candidate, paper);
		}
	}

	private void appendQuestions(Candidate candidate, Paper paper) {
		long count = candidate.getAnswers().stream().filter(candidateAnswer -> candidateAnswer.getQuestion().getId().getPaperId().equals(paper.getId())).count();
		if (count == paper.getQuestionsPerCandidate()) {
			return;
		}
		Random randomizer = new Random(System.currentTimeMillis() + candidate.getId().hashCode());
		Collections.shuffle(paper.getQuestions(), randomizer);
		for (int i = 0; i < paper.getQuestionsPerCandidate(); i++) {
			CandidateAnswer answer = CandidateAnswer.builder()
					.id(CandidateAnswer.Id.builder()
							.candidateId(candidate.getId())
							.paperName(paper.getId().getName())
							.number(i + 1)
							.build())
					.candidate(candidate)
					.question(paper.getQuestions().get(i))
					.build();
			candidate.getAnswers().add(answer);
		}
	}

	public void updateCandidate(Candidate candidate) {
		candidate.setLastUpdated(Time.valueOf(LocalTime.now()));
		candidateRepository.save(candidate);
	}

	public void cronUpdateCandidate(Candidate candidate) {
		candidateRepository.save(candidate);
	}
}
