package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

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
	@Enumerated(EnumType.STRING)
	@Column(name = "GENDER")
	private Gender gender = Gender.UNSPECIFIED;

	@Builder.Default
	@Column(name = "HAS_LOGGED_IN", nullable = false)
	private boolean loggedIn = false;

	@Builder.Default
	@Column(name = "HAS_SUBMITTED", nullable = false)
	private boolean submitted = false;

	private int score() {
		int score = 0;
		for (var answer : answers) {
			if (answer.isCorrect())
				score++;
		}
		return score;
	}

	private int getMaxScore() {
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

	public enum Gender {
		MALE,
		FEMALE,
		UNSPECIFIED
	}
}
