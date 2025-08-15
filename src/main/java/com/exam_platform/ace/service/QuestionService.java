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

	public Question fetchQuestionById(Long examId, String paper, Integer number) {
		return questionRepository.findById_PaperId_ExamIdAndId_PaperId_NameAndId_Number(examId, paper, number).orElse(null);
	}

    public void addImageToQuestionById(Long examId, String paperName, Integer number, MultipartFile image) {
        var question = questionRepository.findById_PaperId_ExamIdAndId_PaperId_NameAndId_Number(examId, paperName, number).orElse(null);
        if (question == null) {
            return;
        }
        try {
            question.saveImage(image);
        } catch (Exception e) {
            e.printStackTrace(System.err);
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
			e.printStackTrace(System.err);
		}
		questionRepository.save(question);
	}

    public void deleteImageInQuestionById(Long examId, String paperName, Integer number) {
        var question = questionRepository.findById_PaperId_ExamIdAndId_PaperId_NameAndId_Number(examId, paperName, number).orElse(null);
        if (question == null)
            return;

        try {
            question.deleteImage();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        questionRepository.save(question);
    }
}
