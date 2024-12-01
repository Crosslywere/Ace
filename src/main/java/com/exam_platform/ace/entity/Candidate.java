package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
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

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateAnswer> answers;

	@Column(name = "EMAIL")
	private String email;

	@Builder.Default
	@Column(name = "NOTIFIED")
	private boolean notified = false;

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

	@Builder.Default
	@Column(name = "HAS_LOGGED_IN", nullable = false)
	private boolean loggedIn = false;

	@Builder.Default
	@Column(name = "HAS_SUBMITTED", nullable = false)
	private boolean submitted = false;

	@Builder.Default
	@Column(name = "PAPER_NAMES")
	private List<String> papers = new ArrayList<>();

	public int score() {
		return (int)answers.stream().filter(CandidateAnswer::isCorrect).count();
	}

	public int getMaxScore(String paperName) {
		return (int)answers.stream().filter(ans -> ans.getQuestion().getId().getPaperId().getName().equals(paperName)).count();
	}

	public int getMaxScore() {
		return answers.size();
	}

	@Override
	public int compareTo(@NotNull Candidate o) {
		return 0;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class Id {
		@Column(name = "EXAM_ID")
		private Long examId;

		@Column(name = "USERNAME")
		private String username;
	}
}
