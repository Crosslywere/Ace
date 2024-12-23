package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
	@Column(name = "SHOW_RESULTS", nullable = false)
	private boolean showResults = false;

	@Builder.Default
	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Paper> papers = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Candidate> candidates = new ArrayList<>();

	@Builder.Default
	@Column(name = "NOTIFY")
	private boolean notify = false;

	@Builder.Default
	@Column(name = "STATE", nullable = false)
	@Enumerated(EnumType.STRING)
	private State state = State.SCHEDULED;
//region How candidates login
	@Builder.Default
	@Column(name = "USERNAME_DESC", nullable = false)
	private String usernameDesc = "Username";

	@Builder.Default
	@Column(name = "PASSWORD_REQUIRED", nullable = false)
	private boolean passwordRequired = false;

	@Builder.Default
	@Column(name = "PASSWORD_DESC")
	private String passwordDesc = null;
//endregion
//region How candidates are exported
	@Builder.Default
	@Embedded
	private CandidateConfig candidateConfig = new CandidateConfig();
//endregion

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getDuration() {
		return duration;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}

	public Time getOpenTime() {
		return openTime;
	}

	public Time getCloseTime() {
		return closeTime;
	}

	public boolean isShowResults() {
		return showResults;
	}

	public List<Paper> getPapers() {
		return papers;
	}

	public List<Candidate> getCandidates() {
		return candidates;
	}

	public boolean isNotify() {
		return notify;
	}

	public State getState() {
		return state;
	}

	public String getUsernameDesc() {
		return usernameDesc;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public String getPasswordDesc() {
		return passwordDesc;
	}

	public CandidateConfig getCandidateConfig() {
		return candidateConfig;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public void setOpenTime(Time openTime) {
		this.openTime = openTime;
	}

	public void setCloseTime(Time closeTime) {
		this.closeTime = closeTime;
	}

	public void setShowResults(boolean showResults) {
		this.showResults = showResults;
	}

	public void setPapers(List<Paper> papers) {
		this.papers = papers;
	}

	public void setCandidates(List<Candidate> candidates) {
		this.candidates = candidates;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setUsernameDesc(String usernameDesc) {
		this.usernameDesc = usernameDesc;
	}

	public void setPasswordRequired(boolean passwordRequired) {
		this.passwordRequired = passwordRequired;
	}

	public void setPasswordDesc(String passwordDesc) {
		this.passwordDesc = passwordDesc;
	}

	public void setCandidateConfig(CandidateConfig candidateConfig) {
		this.candidateConfig = candidateConfig;
	}

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
//region How candidates are exported
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
		@Formula("firstnameDesc != null && !firstnameDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_FIRSTNAME", nullable = false)
		protected boolean firstname = false;
		protected String firstnameDesc;
		@Formula("lastnameDesc != null && !lastnameDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_LASTNAME", nullable = false)
		protected boolean lastname = false;
		protected String lastnameDesc;
		@Formula("otherNamesDesc != null && !otherNamesDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_OTHER_NAMES", nullable = false)
		protected boolean otherNames = false;
		protected String otherNamesDesc;
		@Formula("registrationNumberDesc != null && !registrationNumberDesc.isBlank()")
		@Builder.Default
		@Column(name = "HAS_REGISTRATION_NUMBER", nullable = false)
		protected boolean registrationNumber = false;
		protected String registrationNumberDesc;

		public boolean isEmail() {
			return email;
		}

		public String getEmailDesc() {
			return emailDesc;
		}

		public boolean isPhoneNumber() {
			return phoneNumber;
		}

		public String getPhoneNumberDesc() {
			return phoneNumberDesc;
		}

		public boolean isAddress() {
			return address;
		}

		public String getAddressDesc() {
			return addressDesc;
		}

		public boolean isState() {
			return state;
		}

		public String getStateDesc() {
			return stateDesc;
		}

		public boolean isFirstname() {
			return firstname;
		}

		public String getFirstnameDesc() {
			return firstnameDesc;
		}

		public boolean isLastname() {
			return lastname;
		}

		public String getLastnameDesc() {
			return lastnameDesc;
		}

		public boolean isOtherNames() {
			return otherNames;
		}

		public String getOtherNamesDesc() {
			return otherNamesDesc;
		}

		public boolean isRegistrationNumber() {
			return registrationNumber;
		}

		public String getRegistrationNumberDesc() {
			return registrationNumberDesc;
		}

		public void setEmail(boolean email) {
			this.email = email;
		}

		public void setEmailDesc(String emailDesc) {
			this.emailDesc = emailDesc;
		}

		public void setPhoneNumber(boolean phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public void setPhoneNumberDesc(String phoneNumberDesc) {
			this.phoneNumberDesc = phoneNumberDesc;
		}

		public void setAddress(boolean address) {
			this.address = address;
		}

		public void setAddressDesc(String addressDesc) {
			this.addressDesc = addressDesc;
		}

		public void setState(boolean state) {
			this.state = state;
		}

		public void setStateDesc(String stateDesc) {
			this.stateDesc = stateDesc;
		}

		public void setFirstname(boolean firstname) {
			this.firstname = firstname;
		}

		public void setFirstnameDesc(String firstnameDesc) {
			this.firstnameDesc = firstnameDesc;
		}

		public void setLastname(boolean lastname) {
			this.lastname = lastname;
		}

		public void setLastnameDesc(String lastnameDesc) {
			this.lastnameDesc = lastnameDesc;
		}

		public void setOtherNames(boolean otherNames) {
			this.otherNames = otherNames;
		}

		public void setOtherNamesDesc(String otherNamesDesc) {
			this.otherNamesDesc = otherNamesDesc;
		}

		public void setRegistrationNumber(boolean registrationNumber) {
			this.registrationNumber = registrationNumber;
		}

		public void setRegistrationNumberDesc(String registrationNumberDesc) {
			this.registrationNumberDesc = registrationNumberDesc;
		}
	}
//endregion
}
