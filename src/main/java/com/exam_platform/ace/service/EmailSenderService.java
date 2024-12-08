package com.exam_platform.ace.service;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Async
@Service
@RequiredArgsConstructor
public class EmailSenderService {

	private final JavaMailSender mailSender;

	private final CandidateService candidateService;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMMMM, yyyy");

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm aa");

	@Value("${spring.mail.username}")
	private String senderMail;

	public void sendMail(Candidate candidate, Exam exam) {
		SimpleMailMessage message = new SimpleMailMessage();
		if (senderMail != null && !senderMail.isBlank() && candidate.getEmail() != null && !candidate.getEmail().isBlank()) {
			message.setFrom(senderMail);
			message.setTo(candidate.getEmail());
			message.setSubject("Ace Training Center - Login details for " + exam.getTitle());
			StringBuilder content = new StringBuilder("Login information is surrounded by quote(\") marks. Your login information is as follows\n")
					.append(exam.getUsernameDesc())
					.append(": \"")
					.append(candidate.getId().getUsername())
					.append("\"\n");
			if (exam.isPasswordRequired()) {
				content.append(exam.getPasswordDesc())
						.append(": \"")
						.append(candidate.getPassword())
						.append("\"\n");
			}
			content.append("\nExam Scheduled For: ")
					.append(DATE_FORMAT.format(exam.getScheduledDate()))
					.append("\nExam Opens By: ")
					.append(TIME_FORMAT.format(exam.getOpenTime()))
					.append("\nExam Closes By: ")
					.append(TIME_FORMAT.format(exam.getCloseTime()));
			message.setText(content.toString());
			try {
				mailSender.send(message);
			} catch (Exception e) {
				return;
			}
			candidate.setNotified(true);
			candidateService.cronUpdateCandidate(candidate);
		}
	}
}
