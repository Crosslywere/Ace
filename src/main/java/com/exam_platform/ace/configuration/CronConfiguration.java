package com.exam_platform.ace.configuration;

import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.service.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class CronConfiguration {

	private final ExamService examService;

	// Updates the exam list every 5,000 milliseconds (5 seconds)
	@Scheduled(fixedRate = 30_000)
	public void updateOngoingExams() {
		var exams = examService.getExamsByState(Exam.State.ONGOING);
		for (var exam : exams) {
			if (exam.getCloseTime().after(Time.valueOf(LocalTime.now()))) {
				exam.setState(Exam.State.RECORDED);
				examService.updateExam(exam);
				continue;
			}
			if (exam.getScheduledDate().before(Date.valueOf(LocalDate.now()))) {
				exam.setState(Exam.State.RECORDED);
				examService.updateExam(exam);
			}
		}
	}

	// Updates the exam list every 1 minute from 6:00am to 5:59pm
	@Scheduled(fixedRate = 60_000)
	public void updateScheduledExams() {
		var exams = examService.getExamsByState(Exam.State.SCHEDULED);
		var now = LocalTime.now();
		System.out.println("TimeStamp: " + now);
		for (var exam : exams) {
			if (exam.getScheduledDate().toLocalDate().isEqual(LocalDate.now())) { // If exam is for today
				if ((exam.getOpenTime().toLocalTime().isAfter(now) || exam.getOpenTime().compareTo(Time.valueOf(now)) == 0)
						&& exam.getCloseTime().toLocalTime().isBefore(now)) { // If exam's running time is now
					exam.setState(Exam.State.ONGOING);
					examService.updateExam(exam);
				} else if (exam.getCloseTime().toLocalTime().isAfter(now)) {
					exam.setState(Exam.State.RECORDED);
				}
			} else if (exam.getScheduledDate().toLocalDate().isAfter(LocalDate.now())) {
				exam.setState(Exam.State.RECORDED);
				examService.updateExam(exam);
			}
		}
	}
}
