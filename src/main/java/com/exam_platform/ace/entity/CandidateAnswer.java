package com.exam_platform.ace.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "CANDIDATE_ANSWERS")
public class CandidateAnswer {

	@EmbeddedId
	private Id id;

	@ManyToOne
	@MapsId("candidateId")
	@JoinColumns({
			@JoinColumn(name = "EXAM_ID", referencedColumnName = "EXAM_ID"),
			@JoinColumn(name = "CANDIDATE_USERNAME", referencedColumnName = "USERNAME")
	})
	private Candidate candidate;

	@ManyToOne
	@JoinColumns({
			@JoinColumn(name = "QUESTION_EXAM_ID", referencedColumnName = "EXAM_ID"),
			@JoinColumn(name = "QUESTION_PAPER_NAME", referencedColumnName = "PAPER_NAME"),
			@JoinColumn(name = "QUESTION_NUMBER", referencedColumnName = "QUESTION_NUMBER")
	})
	private Question question;

	@Column(name = "ANSWER")
	private Byte answer;

	public CandidateAnswer() {
	}

	public CandidateAnswer(Id id, Candidate candidate, Question question, Byte answer) {
		this.id = id;
		this.candidate = candidate;
		this.question = question;
		this.answer = answer;
	}

	public Id getId() {
		return id;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public Question getQuestion() {
		return question;
	}

	public Byte getAnswer() {
		return answer;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public void setCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public void setAnswer(Byte answer) {
		this.answer = answer;
	}

	public static CandidateAnswerBuilder builder() {
		return new CandidateAnswerBuilder();
	}

	public static class CandidateAnswerBuilder {

		private Id id;
		private Candidate candidate;
		private Question question;
		private Byte answer;

		public CandidateAnswerBuilder() {
			CandidateAnswer answer = new CandidateAnswer();
			this.id = answer.id;
			this.candidate = answer.candidate;
			this.question = answer.question;
			this.answer = answer.answer;
		}

		public CandidateAnswerBuilder id(Id id) {
			this.id = id;
			return this;
		}

		public CandidateAnswerBuilder candidate(Candidate candidate) {
			this.candidate = candidate;
			return this;
		}

		public CandidateAnswerBuilder question(Question question) {
			this.question = question;
			return this;
		}

		public CandidateAnswerBuilder answer(Byte answer) {
			this.answer = answer;
			return this;
		}

		public CandidateAnswer build() {
			return new CandidateAnswer(id, candidate, question, answer);
		}
	}

	@Embeddable
	public static class Id {
		private Candidate.Id candidateId;

		@Column(name = "PAPER_NAME")
		private String paperName;

		@Column(name = "CANDIDATE_QUESTION_NUMBER")
		private int number;

		public Id() {
		}

		public Id(Candidate.Id candidateId, String paperName, int number) {
			this.candidateId = candidateId;
			this.paperName = paperName;
			this.number = number;
		}

		public Candidate.Id getCandidateId() {
			return candidateId;
		}

		public String getPaperName() {
			return paperName;
		}

		public int getNumber() {
			return number;
		}

		public void setCandidateId(Candidate.Id candidateId) {
			this.candidateId = candidateId;
		}

		public void setPaperName(String paperName) {
			this.paperName = paperName;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public static IdBuilder builder() {
			return new IdBuilder();
		}

		public static class IdBuilder {

			private Candidate.Id candidateId;
			private String paperName;
			private int number;

			public IdBuilder() {
				Id id = new Id();
				this.candidateId = id.candidateId;
				this.paperName = id.paperName;
				this.number = id.number;
			}

			public IdBuilder candidateId(Candidate.Id id) {
				candidateId = id;
				return this;
			}

			public IdBuilder paperName(String paperName) {
				this.paperName = paperName;
				return this;
			}

			public IdBuilder number(int number) {
				this.number = number;
				return this;
			}

			public Id build() {
				return new Id(candidateId, paperName, number);
			}
		}
	}

	public boolean isCorrect() {
		return Objects.equals(answer, question.getAnswerIndex()) && Objects.nonNull(question.getAnswerIndex());
	}
}
