package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Question;
import com.exam_platform.ace.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class QuestionService {

	private final QuestionRepository questionRepository;

	public QuestionService(@Autowired QuestionRepository questionRepository) {
		this.questionRepository = questionRepository;
	}

	public void addImageToQuestionById(Question.Id questionId, MultipartFile image) {
		var question = questionRepository.findById(questionId).orElse(null);
		if (question == null) {
			return;
		}
		try {
			question.saveImage(image);
		} catch (Exception e) {
			//noinspection CallToPrintStackTrace
			e.printStackTrace();
		}
		questionRepository.save(question);
	}

	public void deleteImageInQuestionById(Question.Id questionId) {
		var question = questionRepository.findById(questionId).orElse(null);
		if (question == null) {
			return;
		}
		try {
			question.deleteImage();
		} catch (Exception e) {
			//noinspection CallToPrintStackTrace
			e.printStackTrace();
		}
		questionRepository.save(question);
	}
}
