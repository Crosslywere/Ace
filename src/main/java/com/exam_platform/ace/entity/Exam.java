package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

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
	@Column(name = "PASSWORD_DESC")
	private String passwordDesc = null;

	@Builder.Default
	@Embedded
	private CandidateConfig candidateConfig = new CandidateConfig();

	public void addPaper(Paper paper) {
		if (this.papers == null) {
			this.papers = new ArrayList<>();
		}
		paper.setExam(this);
		this.papers.stream().filter(p -> p.getId().getName().equalsIgnoreCase(paper.getId().getName())).findFirst().ifPresentOrElse(
				p -> paper.getQuestions().forEach(p::addQuestion),
				() -> this.papers.add(paper)
		);
	}

	public void addCandidate(Candidate candidate) {
		if (this.candidates == null) {
			this.candidates = new ArrayList<>();
		}
		candidate.setExam(this);
		this.candidates.stream().filter(c -> c.getId().getUsername().equals(candidate.getId().getUsername())).findFirst().ifPresentOrElse(
				c -> {
					this.candidates.remove(c);
					this.candidates.add(candidate);
				},
				() -> this.candidates.add(candidate)
		);
	}

	public int countCandidatesCompleted() {
		return (int)this.candidates.stream().filter(Candidate::isSubmitted).count();
	}

	public void prepForSave() {
		papers.forEach(paper -> {
			paper.setExam(this);
			paper.prepForSave();
		});
		candidates.forEach(candidate -> {
			candidate.setExam(this);
		});
	}

	public void prepForUpdate() {
		papers.forEach(paper -> {
			paper.getId().setExamId(id);
			paper.prepForUpdate();
		});
		candidates.forEach(candidate -> candidate.getId().setExamId(id));
		prepForSave();
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
		@Formula("emailDesc != null && !emailDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_EMAIL", nullable = false)
		protected boolean email = false;
		protected String emailDesc;
		@Formula("phoneNumberDesc != null && !phoneNumberDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_PHONE_NUMBER", nullable = false)
		protected boolean phoneNumber = false;
		protected String phoneNumberDesc;
		@Formula("addressDesc != null && !addressDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_ADDRESS", nullable = false)
		protected boolean address = false;
		protected String addressDesc;
		@Formula("stateDesc != null && !stateDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_STATE", nullable = false)
		protected boolean state = false;
		protected String stateDesc;
		@Formula("firstnameDesc != null")
		@Builder.Default
		@Column(name = "HAS_FIRSTNAME", nullable = false)
		protected boolean firstname = false;
		protected String firstnameDesc;
		@Formula("lastnameDesc != null")
		@Builder.Default
		@Column(name = "HAS_LASTNAME", nullable = false)
		protected boolean lastname = false;
		protected String lastnameDesc;
		@Formula("otherNamesDesc != null")
		@Builder.Default
		@Column(name = "HAS_OTHER_NAMES", nullable = false)
		protected boolean otherNames = false;
		protected String otherNamesDesc;
	}
}
