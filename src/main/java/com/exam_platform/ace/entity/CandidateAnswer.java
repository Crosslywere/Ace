package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
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

	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class Id {
		private Candidate.Id candidateId;

		@Column(name = "PAPER_NAME")
		private String paperName;

		@Column(name = "CANDIDATE_QUESTION_NUMBER")
		private int number;

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
	}

	public boolean isCorrect() {
		return Objects.equals(answer, question.getAnswerIndex()) && Objects.nonNull(question.getAnswerIndex());
	}
}
