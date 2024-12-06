package com.exam_platform.ace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Data
@ToString(exclude = "paper")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "QUESTIONS")
public class Question implements Comparable<Question> {

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

	@Column(name = "QUESTION", length = 1024)
	private String query;

	@Column(name = "OPTIONS")
	private List<String> options;

	@Column(name = "ANSWER_INDEX", length = 6)
	private Byte answerIndex;

	@Transient
	private MultipartFile imageDocument;

	public void deleteImage() throws Exception {
		if (hasImage) {
			File file = new ClassPathResource("static" + File.separator + "db-images").getFile();
			Path path = Paths.get(file.getCanonicalPath() + File.separator + id.createImageSaveName() + imageSuffix);
			System.out.println(path);
			if (Files.deleteIfExists(path)) {
				hasImage = false;
				imageSuffix = null;
			}
		}
	}

	public void saveImage(MultipartFile image) throws Exception {
		String filename = image.getOriginalFilename();
		if (filename == null) {
			return;
		}
		imageSuffix = filename.substring(filename.lastIndexOf('.'));
		hasImage = true;
		File file = new ClassPathResource("static" + File.separator + "db-images").getFile();
		Path path = Paths.get(file.getCanonicalPath() + File.separator + id.createImageSaveName() + imageSuffix);
		Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public int compareTo(@NotNull Question o) {
		return Integer.compare(this.id.number, o.id.number);
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
