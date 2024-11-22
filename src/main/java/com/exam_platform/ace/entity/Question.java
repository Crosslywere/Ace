package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Data
@ToString(exclude = "paper")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "QUESTIONS")
public class Question {

	@EmbeddedId
	private Id id;

	@MapsId("paperId")
	@ManyToOne
	@JoinColumns({
			@JoinColumn(name = "EXAM_ID", referencedColumnName = "EXAM_ID"),
			@JoinColumn(name = "PAPER_NAME", referencedColumnName = "PAPER_NAME")
	})
	private Paper paper;

	@Column(name = "IMAGE_SUFFIX", length = 5)
	private String imageSuffix;

	@Formula("imageSuffix != null")
	@Column(name = "HAS_IMAGE")
	private boolean hasImage;

	@Column(name = "QUESTION")
	private String query;

	@Column(name = "OPTIONS")
	private List<String> options;

	@Column(name = "ANSWER_INDEX", length = 6)
	private Byte answerIndex;

	public Question deleteImage() throws Exception {
		if (hasImage) {
			File file = new ClassPathResource("static" + File.separator + "db-images").getFile();
			Path path = Paths.get(file.getCanonicalPath() + File.separator + id.createImageSaveName() + imageSuffix);
			System.out.println(path);
			if (Files.deleteIfExists(path)) {
				hasImage = false;
				imageSuffix = null;
				System.out.println("Deleted image");
			}
		}
		return this;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class Id {

		private Paper.Id paperId;

		@Column(name = "QUESTION_NUMBER")
		private Integer number;

		public String createImageSaveName() {
			return paperId.getExamId() +
					paperId.getName() +
					number;
		}
	}
}
