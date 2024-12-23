package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ToString(exclude = "exam")
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

	@Builder.Default
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateAnswer> answers = new ArrayList<>();

	@Column(name = "TIME_IN")
	private Time timeIn;

	@Column(name = "LAST_UPDATED")
	private Time lastUpdated;

	@Builder.Default
	@Column(name = "TIME_USED")
	private float timeUsed = 0.0f;

	@Builder.Default
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

	@Builder.Default
	@Column(name = "HAS_LOGGED_IN", nullable = false)
	private boolean loggedIn = false;

	@Builder.Default
	@Column(name = "HAS_SUBMITTED", nullable = false)
	private boolean submitted = false;

	@Builder.Default
	@Column(name = "PAPER_NAMES")
	private List<String> papers = new ArrayList<>();

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

	public Time getLastUpdated() {
		return lastUpdated;
	}

	public float getTimeUsed() {
		return timeUsed;
	}

	public boolean isNotified() {
		return notified;
	}

	public String getEmail() {
		return email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public String getState() {
		return state;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastUpdated(Time lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public void setTimeUsed(float timeUsed) {
		this.timeUsed = timeUsed;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public void setPapers(List<String> papers) {
		this.papers = papers;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public List<String> getPapers() {
		return papers;
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

	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class Id {
		@Column(name = "EXAM_ID")
		private Long examId;

		@Column(name = "USERNAME")
		private String username;

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
	}
}
