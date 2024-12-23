package com.exam_platform.ace.configuration;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import com.exam_platform.ace.service.EmailSenderService;
import com.exam_platform.ace.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
@EnableScheduling
public class CronConfiguration {

	private final ExamService examService;

	private final EmailSenderService senderService;

	public CronConfiguration(@Autowired ExamService examService, @Autowired EmailSenderService senderService) {
		this.examService = examService;
		this.senderService = senderService;
	}

	@Scheduled(cron = "0 0/30 9-17 * * *")
	public void notifyCandidates() {
		examService.getExamsByState(Exam.State.SCHEDULED).stream().filter(Exam::isNotify).forEach(
				exam -> exam.getCandidates().stream().filter(Candidate::isNotNotified).forEach(
						candidate -> {
							senderService.sendMail(candidate, exam);
						}
				)
		);
	}

	// Updates the exam list every 5,000 milliseconds (5 seconds)
	@Scheduled(fixedRate = 15_000)
	public void updateOngoingExams() {
		var exams = examService.getExamsByState(Exam.State.ONGOING);
		for (var exam : exams) {
			if (exam.getCloseTime().compareTo(Time.valueOf(LocalTime.now())) <= 0) {
				exam.setState(Exam.State.RECORDED);
				examService.updateExam(exam);
				continue;
			}
			if (exam.getScheduledDate().compareTo(Date.valueOf(LocalDate.now())) < 0) {
				exam.setState(Exam.State.RECORDED);
				examService.updateExam(exam);
			}
		}
	}

	// Updates the exam list every 1 minute from 6:00am to 5:59pm
	@Scheduled(fixedRate = 15_000)
	public void updateScheduledExams() {
		var exams = examService.getExamsByState(Exam.State.SCHEDULED);
		var today = Date.valueOf(LocalDate.now());
		var now = Time.valueOf(LocalTime.now());
		for (var exam : exams) {
			if (!exam.getPapers().isEmpty() && !exam.getCandidates().isEmpty()) {
				if (exam.getScheduledDate().compareTo(today) == 0 &&
						(exam.getOpenTime().compareTo(now) <= 0 && exam.getCloseTime().compareTo(now) > 0)) {
					exam.setState(Exam.State.ONGOING);
					examService.updateExam(exam);
					continue;
				} else if (exam.getScheduledDate().compareTo(today) == 0 && exam.getCloseTime().compareTo(now) <= 0) {
					exam.setState(Exam.State.RECORDED);
					examService.updateExam(exam);
				}
				if (exam.getScheduledDate().compareTo(today) < 0) {
					exam.setState(Exam.State.RECORDED);
					examService.updateExam(exam);
				}
			}
		}
	}
}
