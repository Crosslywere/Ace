package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "EXAMS")
public class Exam {

	@Id
	@GeneratedValue
	@Column(name = "EXAM_ID")
	private Long id;

	@Column(name = "TITLE", nullable = false)
	private String title;

	@Builder.Default
	@Column(name = "DURATION", nullable = false)
	private int duration = 60;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Date createdAt;

	@Builder.Default
	@Column(name = "SCHEDULED_DATE", nullable = false)
	private Date scheduledDate = Date.valueOf(LocalDate.now().plusDays(1L));

	@Builder.Default
	@Column(name = "OPEN_TIME", nullable = false)
	private Time openTime = Time.valueOf(LocalTime.of(9, 0, 0));

	@Builder.Default
	@Column(name = "CLOSE_TIME", nullable = false)
	private Time closeTime = Time.valueOf(LocalTime.of(17, 0, 0));

	@Builder.Default
	@Column(name = "PAPERS_PER_CANDIDATE", nullable = false)
	private int papersPerCandidate = 1;

	@Builder.Default
	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Paper> papers = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Candidate> candidates = new ArrayList<>();

	@Builder.Default
	@Column(name = "STATE", nullable = false)
	@Enumerated(EnumType.STRING)
	private State state = State.SCHEDULED;

	@Builder.Default
	@Column(name = "USERNAME_DESC", nullable = false)
	private String usernameDesc = "Username";

	@Builder.Default
	@Column(name = "PASSWORD_REQUIRED", nullable = false)
	private boolean passwordRequired = false;

	@Builder.Default
	@Embedded
	private CandidateConfig candidateConfig = new CandidateConfig();

	public void addPapers(List<Paper> papers) {
		if (this.papers == null) {
			this.papers = new ArrayList<>();
		}
		for(var paper : papers) {
			paper.setExam(this);
		}
		this.papers.addAll(papers);
	}

	public void addPaper(Paper paper) {
		if (this.papers == null) {
			this.papers = new ArrayList<>();
		}
		int index = -1;
		for (int i = 0; i < papers.size(); i++) {
			if (papers.get(i).getId().getName().equals(paper.getId().getName())) {
				index = i;
				break;
			}
		}
		paper.setExam(this);
		if (index == -1) {
			this.papers.add(paper);
		} else {
			this.papers.remove(this.papers.get(index));
			this.papers.add(paper);
		}
	}

	public void addCandidates(List<Candidate> candidates) {
		if (this.candidates == null) {
			this.candidates = new ArrayList<>();
		}
		for (var candidate : candidates) {
			candidate.setExam(this);
		}
		this.candidates.addAll(candidates);
	}

	public void addCandidate(Candidate candidate) {
		if (this.candidates == null) {
			this.candidates = new ArrayList<>();
		}
		if (this.candidates.stream().noneMatch(c -> c.getId().getUsername().equals(candidate.getId().getUsername()))) {
			candidate.setExam(this);
			this.candidates.add(candidate);
		}
	}

	public int countCandidatesCompleted() {
		return (int)this.candidates.stream().filter(Candidate::isSubmitted).count();
	}

	@PrePersist
	public void onCreate() {
		createdAt = new Date(System.currentTimeMillis());
	}

	public enum State {
		SCHEDULED,
		ONGOING,
		RECORDED;
	}

	@Data
	@SuperBuilder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class CandidateConfig {
		@Builder.Default
		@Column(name = "HAS_EMAIL", nullable = false)
		protected boolean email = false;
		protected String emailDesc;
		@Builder.Default
		@Column(name = "HAS_GENDER", nullable = false)
		protected boolean gender = false;
		protected String genderDesc;
		@Builder.Default
		@Column(name = "HAS_PHONE_NUMBER", nullable = false)
		protected boolean phoneNumber = false;
		protected String phoneNumberDesc;
		@Builder.Default
		@Column(name = "HAS_ADDRESS", nullable = false)
		protected boolean address = false;
		protected String addressDesc;
		@Builder.Default
		@Column(name = "HAS_STATE", nullable = false)
		protected boolean state = false;
		protected String stateDesc;
		@Builder.Default
		@Column(name = "HAS_FIRSTNAME", nullable = false)
		protected boolean firstname = false;
		protected String firstnameDesc;
		@Builder.Default
		@Column(name = "HAS_LASTNAME", nullable = false)
		protected boolean lastname = false;
		protected String lastnameDesc;
		@Builder.Default
		@Column(name = "HAS_OTHER_NAMES", nullable = false)
		protected boolean otherNames = false;
		protected String otherNamesDesc;
	}
}
