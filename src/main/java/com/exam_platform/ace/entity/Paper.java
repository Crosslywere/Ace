package com.exam_platform.ace.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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
	private boolean mandatory = false;

	@OneToMany(mappedBy = "paper", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Question> questions = new ArrayList<>();

	public Paper() {
	}

	public Paper(Id id, Exam exam, int questionsPerCandidate, boolean mandatory, List<Question> questions) {
		this.id = id;
		this.exam = exam;
		this.questionsPerCandidate = questionsPerCandidate;
		this.mandatory = mandatory;
		this.questions = questions;
	}

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

	public static PaperBuilder builder() {
		return new PaperBuilder();
	}

	public static class PaperBuilder {

		private Id id;
		private Exam exam;
		private int questionsPerCandidate;
		private boolean mandatory;
		private List<Question> questions;

		public PaperBuilder() {
			Paper paper = new Paper();
			this.id = paper.id;
			this.exam = paper.exam;
			this.questionsPerCandidate = paper.questionsPerCandidate;
			this.mandatory = paper.mandatory;
			this.questions = paper.questions;
		}

		public PaperBuilder id(Id id) {
			this.id = id;
			return this;
		}

		public PaperBuilder exam(Exam exam) {
			this.exam = exam;
			return this;
		}

		public PaperBuilder questionsPerCandidate(int questionsPerCandidate) {
			this.questionsPerCandidate = questionsPerCandidate;
			return this;
		}

		public PaperBuilder mandatory(boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

		public PaperBuilder questions(List<Question> questions) {
			this.questions = questions;
			return this;
		}

		public Paper build() {
			return new Paper(id, exam, questionsPerCandidate, mandatory, questions);
		}
	}

	@Embeddable
	public static class Id {

		@Column(name = "EXAM_ID")
		private Long examId;

		@Column(name = "PAPER_NAME")
		private String name;

		public Id() {
		}

		public Id(Long examId, String name) {
			this.examId = examId;
			this.name = name;
		}

		public Long getExamId() {
			return examId;
		}

		public void setExamId(Long examId) {
			this.examId = examId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public static IdBuilder builder() {
			return new IdBuilder();
		}

		public static class IdBuilder {

			private Long examId;
			private String name;

			public IdBuilder() {
				Id id = new Id();
				this.examId = id.examId;
				this.name = id.name;
			}

			public IdBuilder examId(Long examId) {
				this.examId = examId;
				return this;
			}

			public IdBuilder name(String name) {
				this.name = name;
				return this;
			}

			public Id build() {
				return new Id();
			}
		}
	}
}
