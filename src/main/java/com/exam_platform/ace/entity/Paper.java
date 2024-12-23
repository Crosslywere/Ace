package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

	@Builder.Default
	@Column(name = "MANDATORY", nullable = false)
	private boolean mandatory = false;

	@Builder.Default
	@OneToMany(mappedBy = "paper", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions = new ArrayList<>();

	public void addQuestion(Question question) {
		if (this.questions == null) {
			this.questions = new ArrayList<>();
		}
		question.setPaper(this);
		this.questions.stream().filter(q -> q.getId().getNumber().equals(question.getId().getNumber())).findFirst().ifPresentOrElse(
				q -> {
					q.setQuery(question.getQuery());
					if (question.getOptions() != null && !question.getOptions().isEmpty())
						q.setOptions(question.getOptions());
					if (question.getAnswerIndex() != null)
						q.setAnswerIndex(question.getAnswerIndex());
					try {
						q.deleteImage();
					} catch (Exception e) {
						//noinspection CallToPrintStackTrace
						e.printStackTrace();
					}
				},
				() -> this.questions.add(question)
		);
	}

	public void prepForSave() {
		questions.forEach(question -> {
			question.setPaper(this);
		});
	}

	public void prepForUpdate() {
		questions.forEach(question -> {
			question.getId().setPaperId(id);
		});
	}

	public Id getId() {
		return id;
	}

	public Exam getExam() {
		return exam;
	}

	public int getQuestionsPerCandidate() {
		return questionsPerCandidate;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public void setExam(Exam exam) {
		this.exam = exam;
	}

	public void setQuestionsPerCandidate(int questionsPerCandidate) {
		this.questionsPerCandidate = questionsPerCandidate;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class Id {

		@Column(name = "EXAM_ID")
		private Long examId;

		@Column(name = "PAPER_NAME")
		private String name;

		public Long getExamId() {
			return examId;
		}

		public String getName() {
			return name;
		}

		public void setExamId(Long examId) {
			this.examId = examId;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
