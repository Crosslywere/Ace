package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "CANDIDATES")
public class Candidate implements Comparable<Candidate> {

	@EmbeddedId
	private Id id;

	@ManyToOne
	@MapsId("examId")
	@JoinColumn(name = "EXAM_ID", referencedColumnName = "EXAM_ID")
	private Exam exam;

	@Column(name = "PASSWORD")
	private String password;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateAnswer> answers = new ArrayList<>();

	@Column(name = "TIME_IN")
	private Time timeIn;

	@Column(name = "LAST_UPDATED")
	private Time lastUpdated;

	@Column(name = "TIME_USED")
	private float timeUsed = 0.0f;

	@Column(name = "NOTIFIED")
	private boolean notified = false;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "PHONE_NUMBER")
	private String phoneNumber;

	@Column(name = "ADDRESS")
	private String address;

	@Column(name = "STATE")
	private String state;

	@Column(name = "FIRST_NAME")
	private String firstname;

	@Column(name = "LAST_NAME")
	private String lastname;

	@Column(name = "OTHER_NAMES")
	private String otherNames;

	@Column(name = "REGISTRATION_NUMBER")
	private String registrationNumber;

	@Column(name = "HAS_LOGGED_IN", nullable = false)
	private boolean loggedIn = false;

	@Column(name = "HAS_SUBMITTED", nullable = false)
	private boolean submitted = false;

	@Column(name = "PAPER_NAMES")
	private List<String> papers = new ArrayList<>();

	public Candidate() {
	}

	public Candidate(Id id, Exam exam, String password, List<CandidateAnswer> answers, Time timeIn, Time lastUpdated, float timeUsed, boolean notified, String email, String phoneNumber, String address, String state, String firstname, String lastname, String otherNames, String registrationNumber, boolean loggedIn, boolean submitted, List<String> papers) {
		this.id = id;
		this.exam = exam;
		this.password = password;
		this.answers = answers;
		this.timeIn = timeIn;
		this.lastUpdated = lastUpdated;
		this.timeUsed = timeUsed;
		this.notified = notified;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.address = address;
		this.state = state;
		this.firstname = firstname;
		this.lastname = lastname;
		this.otherNames = otherNames;
		this.registrationNumber = registrationNumber;
		this.loggedIn = loggedIn;
		this.submitted = submitted;
		this.papers = papers;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public Exam getExam() {
		return exam;
	}

	public void setExam(Exam exam) {
		this.exam = exam;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<CandidateAnswer> getAnswers() {
		return answers;
	}

	public void setAnswers(List<CandidateAnswer> answers) {
		this.answers = answers;
	}

	public Time getTimeIn() {
		return timeIn;
	}

	public void setTimeIn(Time timeIn) {
		this.timeIn = timeIn;
	}

	public Time getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Time lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public float getTimeUsed() {
		return timeUsed;
	}

	public void setTimeUsed(float timeUsed) {
		this.timeUsed = timeUsed;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public List<String> getPapers() {
		return papers;
	}

	public void setPapers(List<String> papers) {
		this.papers = papers;
	}

	public boolean isNotNotified() {
		return !notified;
	}

	public List<CandidateAnswer> getPaperAnswers(String paperName) {
		return answers.stream().filter(answer -> answer.getId().getPaperName().equals(paperName)).sorted(Comparator.comparingInt(answer -> answer.getId().getNumber())).toList();
	}

	public int score(String paperName) {
		return (int) answers.stream().filter(answer -> answer.getId().getPaperName().equals(paperName) && answer.isCorrect()).count();
	}

	public int score() {
		return (int) answers.stream().filter(CandidateAnswer::isCorrect).count();
	}

	public int getMaxScore(String paperName) {
		return (int) answers.stream().filter(ans -> ans.getId().getPaperName().equals(paperName)).count();
	}

	public int getMaxScore() {
		return answers.size();
	}

	public String getFormattedScore(String paperName, boolean percentile) {
		if (percentile) {
			return String.format("%.2f", (score(paperName) / (getMaxScore(paperName) * 1.0f)) * 100);
		}
		return score(paperName) + "/" + getMaxScore(paperName);
	}

	public String getFormattedScore(boolean percentile) {
		if (percentile) {
			return String.format("%.2f", (score() / (getMaxScore() * 1.0f)) * 100);
		}
		return String.valueOf(score());
	}

	@Override
	public int compareTo(@NotNull Candidate o) {
		return Integer.compare(score(), o.score());
	}

	public static CandidateBuilder builder() {
		return new CandidateBuilder();
	}

	public static class CandidateBuilder {
		private Candidate.Id id;
		private Exam exam;
		private String password;
		private List<CandidateAnswer> answers;
		private Time timeIn;
		private Time lastUpdated;
		private float timeUsed;
		private boolean notified;
		private String email;
		private String phoneNumber;
		private String address;
		private String state;
		private String firstname;
		private String lastname;
		private String otherNames;
		private String registrationNumber;
		private boolean loggedIn;
		private boolean submitted;
		private List<String> papers;

		public CandidateBuilder() {
			Candidate candidate = new Candidate();
			this.id = candidate.id;
			this.exam = candidate.exam;
			this.password = candidate.password;
			this.answers = candidate.answers;
			this.timeIn = candidate.timeIn;
			this.lastUpdated = candidate.lastUpdated;
			this.timeUsed = candidate.timeUsed;
			this.notified = candidate.notified;
			this.email = candidate.email;
			this.phoneNumber = candidate.phoneNumber;
			this.address = candidate.address;
			this.state = candidate.state;
			this.firstname = candidate.firstname;
			this.lastname = candidate.lastname;
			this.otherNames = candidate.otherNames;
			this.registrationNumber = candidate.registrationNumber;
			this.loggedIn = candidate.loggedIn;
			this.submitted = candidate.submitted;
			this.papers = candidate.papers;
		}

		public CandidateBuilder id(Candidate.Id id) {
			this.id = id;
			return this;
		}

		public CandidateBuilder exam(Exam exam) {
			this.exam = exam;
			return this;
		}

		public CandidateBuilder password(String password) {
			this.password = password;
			return this;
		}

		public CandidateBuilder answers(List<CandidateAnswer> answers) {
			this.answers = answers;
			return this;
		}

		public CandidateBuilder timeId(Time timeIn) {
			this.timeIn = timeIn;
			return this;
		}

		public CandidateBuilder lastUpdated(Time lastUpdated) {
			this.lastUpdated = lastUpdated;
			return this;
		}

		public CandidateBuilder timeUsed(float timeUsed) {
			this.timeUsed = timeUsed;
			return this;
		}

		public CandidateBuilder notified(boolean notified) {
			this.notified = notified;
			return this;
		}

		public CandidateBuilder email(String email) {
			this.email = email;
			return this;
		}

		public CandidateBuilder phoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public CandidateBuilder address(String address) {
			this.address = address;
			return this;
		}

		public CandidateBuilder state(String state) {
			this.state = state;
			return this;
		}

		public CandidateBuilder firstname(String firstname) {
			this.firstname = firstname;
			return this;
		}

		public CandidateBuilder lastname(String lastname) {
			this.lastname = lastname;
			return this;
		}

		public CandidateBuilder otherNames(String otherNames) {
			this.otherNames = otherNames;
			return this;
		}

		public CandidateBuilder registrationNumber(String registrationNumber) {
			this.registrationNumber = registrationNumber;
			return this;
		}

		public CandidateBuilder loggedIn(boolean loggedIn) {
			this.loggedIn = loggedIn;
			return this;
		}

		public CandidateBuilder submitted(boolean submitted) {
			this.submitted = submitted;
			return this;
		}

		public CandidateBuilder papers(List<String> papers) {
			this.papers = papers;
			return this;
		}

		public Candidate build() {
			return new Candidate(id, exam, password, answers, timeIn, lastUpdated, timeUsed, notified, email, phoneNumber, address, state, firstname, lastname, otherNames, registrationNumber, loggedIn, submitted, papers);
		}
	}

	@Embeddable
	public static class Id {
		@Column(name = "EXAM_ID")
		private Long examId;

		@Column(name = "USERNAME")
		private String username;

		public Id() {
		}

		public Id(Long examId, String username) {
			this.examId = examId;
			this.username = username;
		}

		public Long getExamId() {
			return examId;
		}

		public void setExamId(Long examId) {
			this.examId = examId;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public static IdBuilder builder() {
			return new IdBuilder();
		}

		public static class IdBuilder {
			private Long examId;
			private String username;

			public IdBuilder() {
				Id id = new Id();
				this.examId = id.examId;
				this.username = id.username;
			}

			public IdBuilder examId(Long examId) {
				this.examId = examId;
				return this;
			}

			public IdBuilder username(String username) {
				this.username = username;
				return this;
			}

			public Id build() {
				return new Id(examId, username);
			}
		}
	}
}
