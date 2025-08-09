package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "EXAMS")
public class Exam {

	@Id
	@GeneratedValue
	@Column(name = "EXAM_ID")
	private Long id;

	@Column(name = "TITLE", nullable = false)
	private String title;

	@Column(name = "DURATION", nullable = false)
	private int duration = 60;

	@Column(name = "MIN_DURATION", nullable = false)
	private int minDuration = 1;

	@Column(name = "CREATED_AT", nullable = false, updatable = false)
	private Date createdAt;

	@Column(name = "SCHEDULED_DATE", nullable = false)
	private Date scheduledDate = Date.valueOf(LocalDate.now().plusDays(1L));

	@Column(name = "OPEN_TIME", nullable = false)
	private Time openTime = Time.valueOf(LocalTime.of(9, 0, 0));

	@Column(name = "CLOSE_TIME", nullable = false)
	private Time closeTime = Time.valueOf(LocalTime.of(17, 0, 0));

	@Column(name = "SHOW_RESULTS", nullable = false)
	private boolean showResults = false;

	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Paper> papers = new ArrayList<>();

	@OneToMany(mappedBy = "exam", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Candidate> candidates = new ArrayList<>();

	@Column(name = "NOTIFY")
	private boolean notify = false;

	@Column(name = "STATE", nullable = false)
	@Enumerated(EnumType.STRING)
	private State state = State.SCHEDULED;
//region How candidates login
	@Column(name = "USERNAME_DESC", nullable = false)
	private String usernameDesc = "Username";

	@Column(name = "PASSWORD_REQUIRED", nullable = false)
	private boolean passwordRequired = false;

	@Column(name = "PASSWORD_DESC")
	private String passwordDesc = null;
//endregion
//region How candidates are exported
	@Embedded
	private CandidateConfig candidateConfig = new CandidateConfig();
//endregion

	public Exam() {
	}

	public Exam(Long id, String title, int duration, int minDuration, Date createdAt, Date scheduledDate, Time openTime, Time closeTime, boolean showResults, List<Paper> papers, List<Candidate> candidates, boolean notify, State state, String usernameDesc, boolean passwordRequired, String passwordDesc, CandidateConfig candidateConfig) {
		this.id = id;
		this.title = title;
		this.duration = duration;
		this.minDuration = minDuration;
		this.createdAt = createdAt;
		this.scheduledDate = scheduledDate;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.showResults = showResults;
		this.papers = papers;
		this.candidates = candidates;
		this.notify = notify;
		this.state = state;
		this.usernameDesc = usernameDesc;
		this.passwordRequired = passwordRequired;
		this.passwordDesc = passwordDesc;
		this.candidateConfig = candidateConfig;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getMinDuration() {
		return minDuration;
	}

	public void setMinDuration(int minDuration) {
		this.minDuration = minDuration;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Date scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public Time getOpenTime() {
		return openTime;
	}

	public void setOpenTime(Time openTime) {
		this.openTime = openTime;
	}

	public Time getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Time closeTime) {
		this.closeTime = closeTime;
	}

	public boolean isShowResults() {
		return showResults;
	}

	public void setShowResults(boolean showResults) {
		this.showResults = showResults;
	}

	public List<Paper> getPapers() {
		return papers;
	}

	public void setPapers(List<Paper> papers) {
		this.papers = papers;
	}

	public List<Candidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<Candidate> candidates) {
		this.candidates = candidates;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getUsernameDesc() {
		return usernameDesc;
	}

	public void setUsernameDesc(String usernameDesc) {
		this.usernameDesc = usernameDesc;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public void setPasswordRequired(boolean passwordRequired) {
		this.passwordRequired = passwordRequired;
	}

	public String getPasswordDesc() {
		return passwordDesc;
	}

	public void setPasswordDesc(String passwordDesc) {
		this.passwordDesc = passwordDesc;
	}

	public CandidateConfig getCandidateConfig() {
		return candidateConfig;
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

	public static ExamBuilder builder() {
		return new ExamBuilder();
	}

	public static class ExamBuilder {

		private Long id;
		private String title;
		private int duration;
		private int minDuration;
		private Date createdAt;
		private Date scheduledDate;
		private Time openTime;
		private Time closeTime;
		private boolean showResults;
		private List<Paper> papers;
		private List<Candidate> candidates;
		private boolean notify;
		private State state;
		private String usernameDesc;
		private boolean passwordRequired;
		private String passwordDesc;
		private CandidateConfig candidateConfig;

		public ExamBuilder() {
			Exam exam = new Exam();
			this.id = exam.id;
			this.title = exam.title;
			this.duration = exam.duration;
			this.minDuration = exam.minDuration;
			this.createdAt = exam.createdAt;
			this.scheduledDate = exam.scheduledDate;
			this.openTime = exam.openTime;
			this.closeTime = exam.closeTime;
			this.showResults = exam.showResults;
			this.papers = exam.papers;
			this.candidates = exam.candidates;
			this.notify = exam.notify;
			this.state = exam.state;
			this.usernameDesc = exam.usernameDesc;
			this.passwordRequired = exam.passwordRequired;
			this.passwordDesc = exam.passwordDesc;
			this.candidateConfig = exam.candidateConfig;
		}

		public ExamBuilder id(Long id) {
			this.id = id;
			return this;
		}

		public ExamBuilder title(String title) {
			this.title = title;
			return this;
		}

		public ExamBuilder duration(int duration) {
			this.duration = duration;
			return this;
		}

		public ExamBuilder minDuration(int minDuration) {
			this.minDuration = minDuration;
			return this;
		}

		public ExamBuilder createdAt(Date createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public ExamBuilder scheduledDate(Date scheduledDate) {
			this.scheduledDate = scheduledDate;
			return this;
		}

		public ExamBuilder openTime(Time openTime) {
			this.openTime = openTime;
			return this;
		}

		public ExamBuilder closeTime(Time closeTime) {
			this.closeTime = closeTime;
			return this;
		}

		public ExamBuilder showResults(boolean showResults) {
			this.showResults = showResults;
			return this;
		}

		public ExamBuilder papers(List<Paper> papers) {
			this.papers = papers;
			return this;
		}

		public ExamBuilder candidates(List<Candidate> candidates) {
			this.candidates = candidates;
			return this;
		}

		public ExamBuilder notify(boolean notify) {
			this.notify = notify;
			return this;
		}

		public ExamBuilder state(State state) {
			this.state = state;
			return this;
		}

		public ExamBuilder usernameDesc(String usernameDesc) {
			this.usernameDesc = usernameDesc;
			return this;
		}

		public ExamBuilder passwordRequired(boolean passwordRequired) {
			this.passwordRequired = passwordRequired;
			return this;
		}

		public ExamBuilder passwordDesc(String passwordDesc) {
			this.passwordDesc = passwordDesc;
			return this;
		}

		public ExamBuilder candidateConfig(CandidateConfig candidateConfig) {
			this.candidateConfig = candidateConfig;
			return this;
		}

		public Exam build() {
			return new Exam(id, title, duration, minDuration, createdAt, scheduledDate, openTime, closeTime, showResults, papers, candidates, notify, state, usernameDesc, passwordRequired, passwordDesc, candidateConfig);
		}
	}

	@Embeddable
	public static class CandidateConfig {
		@Formula("emailDesc != null && !emailDesc.isBlank()")
		@Column(name = "HAS_EMAIL", nullable = false)
		protected boolean email = false;
		protected String emailDesc;
		@Formula("phoneNumberDesc != null && !phoneNumberDesc.isBlank()")
		@Column(name = "HAS_PHONE_NUMBER", nullable = false)
		protected boolean phoneNumber = false;
		protected String phoneNumberDesc;
		@Formula("addressDesc != null && !addressDesc.isBlank()")
		@Column(name = "HAS_ADDRESS", nullable = false)
		protected boolean address = false;
		protected String addressDesc;
		@Formula("stateDesc != null && !stateDesc.isBlank()")
		@Column(name = "HAS_STATE", nullable = false)
		protected boolean state = false;
		protected String stateDesc;
		@Formula("firstnameDesc != null && !firstnameDesc.isBlank()")
		@Column(name = "HAS_FIRSTNAME", nullable = false)
		protected boolean firstname = false;
		protected String firstnameDesc;
		@Formula("lastnameDesc != null && !lastnameDesc.isBlank()")
		@Column(name = "HAS_LASTNAME", nullable = false)
		protected boolean lastname = false;
		protected String lastnameDesc;
		@Formula("otherNamesDesc != null && !otherNamesDesc.isBlank()")
		@Column(name = "HAS_OTHER_NAMES", nullable = false)
		protected boolean otherNames = false;
		protected String otherNamesDesc;
		@Formula("registrationNumberDesc != null && !registrationNumberDesc.isBlank()")
		@Column(name = "HAS_REGISTRATION_NUMBER", nullable = false)
		protected boolean registrationNumber = false;
		protected String registrationNumberDesc;

		public CandidateConfig() {
		}

		public CandidateConfig(boolean email, String emailDesc, boolean phoneNumber, String phoneNumberDesc, boolean address, String addressDesc, boolean state, String stateDesc, boolean firstname, String firstnameDesc, boolean lastname, String lastnameDesc, boolean otherNames, String otherNamesDesc, boolean registrationNumber, String registrationNumberDesc) {
			this.email = email;
			this.emailDesc = emailDesc;
			this.phoneNumber = phoneNumber;
			this.phoneNumberDesc = phoneNumberDesc;
			this.address = address;
			this.addressDesc = addressDesc;
			this.state = state;
			this.stateDesc = stateDesc;
			this.firstname = firstname;
			this.firstnameDesc = firstnameDesc;
			this.lastname = lastname;
			this.lastnameDesc = lastnameDesc;
			this.otherNames = otherNames;
			this.otherNamesDesc = otherNamesDesc;
			this.registrationNumber = registrationNumber;
			this.registrationNumberDesc = registrationNumberDesc;
		}

		public boolean isEmail() {
			return email;
		}

		public void setEmail(boolean email) {
			this.email = email;
		}

		public String getEmailDesc() {
			return emailDesc;
		}

		public void setEmailDesc(String emailDesc) {
			this.emailDesc = emailDesc;
		}

		public boolean isPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(boolean phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getPhoneNumberDesc() {
			return phoneNumberDesc;
		}

		public void setPhoneNumberDesc(String phoneNumberDesc) {
			this.phoneNumberDesc = phoneNumberDesc;
		}

		public boolean isAddress() {
			return address;
		}

		public void setAddress(boolean address) {
			this.address = address;
		}

		public String getAddressDesc() {
			return addressDesc;
		}

		public void setAddressDesc(String addressDesc) {
			this.addressDesc = addressDesc;
		}

		public boolean isState() {
			return state;
		}

		public void setState(boolean state) {
			this.state = state;
		}

		public String getStateDesc() {
			return stateDesc;
		}

		public void setStateDesc(String stateDesc) {
			this.stateDesc = stateDesc;
		}

		public boolean isFirstname() {
			return firstname;
		}

		public void setFirstname(boolean firstname) {
			this.firstname = firstname;
		}

		public String getFirstnameDesc() {
			return firstnameDesc;
		}

		public void setFirstnameDesc(String firstnameDesc) {
			this.firstnameDesc = firstnameDesc;
		}

		public boolean isLastname() {
			return lastname;
		}

		public void setLastname(boolean lastname) {
			this.lastname = lastname;
		}

		public String getLastnameDesc() {
			return lastnameDesc;
		}

		public void setLastnameDesc(String lastnameDesc) {
			this.lastnameDesc = lastnameDesc;
		}

		public boolean isOtherNames() {
			return otherNames;
		}

		public void setOtherNames(boolean otherNames) {
			this.otherNames = otherNames;
		}

		public String getOtherNamesDesc() {
			return otherNamesDesc;
		}

		public void setOtherNamesDesc(String otherNamesDesc) {
			this.otherNamesDesc = otherNamesDesc;
		}

		public boolean isRegistrationNumber() {
			return registrationNumber;
		}

		public void setRegistrationNumber(boolean registrationNumber) {
			this.registrationNumber = registrationNumber;
		}

		public String getRegistrationNumberDesc() {
			return registrationNumberDesc;
		}

		public void setRegistrationNumberDesc(String registrationNumberDesc) {
			this.registrationNumberDesc = registrationNumberDesc;
		}

		public static CandidateConfigBuilder builder() {
			return new CandidateConfigBuilder();
		}

		public static class CandidateConfigBuilder {

			protected boolean email;
			protected String emailDesc;
			protected boolean phoneNumber;
			protected String phoneNumberDesc;
			protected boolean address;
			protected String addressDesc;
			protected boolean state;
			protected String stateDesc;
			protected boolean firstname;
			protected String firstnameDesc;
			protected boolean lastname;
			protected String lastnameDesc;
			protected boolean otherNames;
			protected String otherNamesDesc;
			protected boolean registrationNumber;
			protected String registrationNumberDesc;

			public CandidateConfigBuilder() {
				CandidateConfig candidateConfig = new CandidateConfig();
				this.email = candidateConfig.email;
				this.emailDesc = candidateConfig.emailDesc;
				this.phoneNumber = candidateConfig.phoneNumber;
				this.phoneNumberDesc = candidateConfig.phoneNumberDesc;
				this.address = candidateConfig.address;
				this.addressDesc = candidateConfig.addressDesc;
				this.state = candidateConfig.state;
				this.stateDesc = candidateConfig.stateDesc;
				this.firstname = candidateConfig.firstname;
				this.firstnameDesc = candidateConfig.firstnameDesc;
				this.lastname = candidateConfig.lastname;
				this.lastnameDesc = candidateConfig.lastnameDesc;
				this.otherNames = candidateConfig.otherNames;
				this.otherNamesDesc = candidateConfig.otherNamesDesc;
				this.registrationNumber = candidateConfig.registrationNumber;
				this.registrationNumberDesc = candidateConfig.registrationNumberDesc;
			}

			public CandidateConfigBuilder email(boolean email) {
				this.email = email;
				return this;
			}

			public CandidateConfigBuilder emailDesc(String emailDesc) {
				this.emailDesc = emailDesc;
				return this;
			}

			public CandidateConfigBuilder phoneNumber(boolean phoneNumber) {
				this.phoneNumber = phoneNumber;
				return this;
			}

			public CandidateConfigBuilder phoneNumberDesc(String phoneNumberDesc) {
				this.phoneNumberDesc = phoneNumberDesc;
				return this;
			}

			public CandidateConfigBuilder address(boolean address) {
				this.address = address;
				return this;
			}

			public CandidateConfigBuilder addressDesc(String addressDesc) {
				this.addressDesc = addressDesc;
				return this;
			}

			public CandidateConfigBuilder state(boolean state) {
				this.state = state;
				return this;
			}

			public CandidateConfigBuilder stateDesc(String stateDesc) {
				this.stateDesc = stateDesc;
				return this;
			}

			public CandidateConfigBuilder firstname(boolean firstname) {
				this.firstname = firstname;
				return this;
			}

			public CandidateConfigBuilder firstnameDesc(String firstnameDesc) {
				this.firstnameDesc = firstnameDesc;
				return this;
			}

			public CandidateConfigBuilder lastname(boolean lastname) {
				this.lastname = lastname;
				return this;
			}

			public CandidateConfigBuilder lastnameDesc(String lastnameDesc) {
				this.lastnameDesc = lastnameDesc;
				return this;
			}

			public CandidateConfigBuilder otherNames(boolean otherNames) {
				this.otherNames = otherNames;
				return this;
			}

			public CandidateConfigBuilder otherNamesDesc(String otherNamesDesc) {
				this.otherNamesDesc = otherNamesDesc;
				return this;
			}

			public CandidateConfigBuilder registrationNumber(boolean registrationNumber) {
				this.registrationNumber = registrationNumber;
				return this;
			}

			public CandidateConfigBuilder registrationNumberDesc(String registrationNumberDesc) {
				this.registrationNumberDesc = registrationNumberDesc;
				return this;
			}

			public CandidateConfig build() {
				return new CandidateConfig(email, emailDesc, phoneNumber, phoneNumberDesc, address, addressDesc, state, stateDesc, firstname, firstnameDesc, lastname, lastnameDesc, otherNames, otherNamesDesc, registrationNumber, registrationNumberDesc);
			}
		}
	}
//endregion
}
