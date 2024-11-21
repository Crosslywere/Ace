package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@ToString(exclude = "exam")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PAPERS")
public class Paper {

	@EmbeddedId
	private Id id;

	@ManyToOne
	@MapsId("examId")
	@JoinColumn(name = "EXAM_ID", nullable = false)
	private Exam exam;

	@Column(name = "QUESTIONS_PER_CANDIDATE", nullable = false)
	private int questionsPerCandidate;

	@Column(name = "MANDATORY", nullable = false)
	private boolean mandatory;

	@OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions;

	public void addQuestions(List<Question> questions) {
		if (this.questions == null) {
			this.questions = new ArrayList<>();
		}
		for (var question : questions) {
			question.setPaper(this);
		}
		this.questions.addAll(questions);
	}

	public void addQuestion(Question question) {
		if (this.questions == null) {
			this.questions = new ArrayList<>();
		}
		if (this.questions.stream().noneMatch(comp -> Objects.equals(comp.getId().getNumber(), question.getId().getNumber()))) {
			question.setPaper(this);
			this.questions.add(question);
		}
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class Id {

		@Column(name = "EXAM_ID")
		private Long examId;

		@Column(name = "PAPER_NAME")
		private String name;
	}
}
