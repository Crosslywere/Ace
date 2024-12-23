package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.CandidateAnswer;
import com.exam_platform.ace.repository.CandidateAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandidateAnswerService {

	private final CandidateAnswerRepository answerRepository;

	public CandidateAnswerService(@Autowired CandidateAnswerRepository answerRepository) {
		this.answerRepository = answerRepository;
	}

	public void setAnswer(CandidateAnswer.Id answerId, Byte answer) {
		var candidateAnswer = answerRepository.findById(answerId).orElse(null);
		if (candidateAnswer == null) {
			return;
		}
		candidateAnswer.setAnswer(answer);
		answerRepository.save(candidateAnswer);
	}

	public CandidateAnswer getCandidateAnswerById(CandidateAnswer.Id answerId) {
		return answerRepository.findById(answerId).orElse(null);
	}
}
