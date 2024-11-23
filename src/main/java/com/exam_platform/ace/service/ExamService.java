package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

	private final ExamRepository examRepository;

	public List<Exam> getExamsByState(Exam.State state, Pageable pageable) {
		return examRepository.findByState(state, pageable);
	}

	public List<Exam> getExamsByState(Exam.State state) {
		return examRepository.findByState(state);
	}

	public long countExamsByState(Exam.State state) {
		return examRepository.countByState(state);
	}

	public Exam createExam(@NotNull Exam exam) {
		Assert.isNull(exam.getId(), "Attempting to create an exam with an ID (" + exam.getId() + ") this could overwrite another exam! Consider using update instead.");
		return examRepository.save(exam);
	}

	public void updateExam(@NotNull Exam exam) {
		Assert.notNull(exam.getId(), "Attempting to update an exam without an ID this can create a new exam! If this is your intention consider using create instead.");
		examRepository.save(exam);
	}

	public Exam getExamById(Long examId) {
		return examRepository.findById(examId).orElse(null);
	}

	public List<Exam> getExamsByTitle(String title) {
		return examRepository.findByTitleContainingIgnoreCase(title);
	}

	public void stopExamById(Long examId) {
		var exam = examRepository.findById(examId).orElse(null);
		if (exam != null && exam.getState() == Exam.State.ONGOING) {
			exam.setCloseTime(Time.valueOf(LocalTime.now()));
			exam.setState(Exam.State.RECORDED);
			examRepository.save(exam);
		}
	}
}
