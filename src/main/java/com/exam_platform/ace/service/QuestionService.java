package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Question;
import com.exam_platform.ace.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class QuestionService {

	private final QuestionRepository questionRepository;

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
