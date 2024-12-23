package com.exam_platform.ace.entity;

import jakarta.persistence.*;
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

	public Question() {
	}

	public Question(Id id, Paper paper, String imageSuffix, boolean hasImage, String query, List<String> options, Byte answerIndex, MultipartFile imageDocument) {
		this.id = id;
		this.paper = paper;
		this.imageSuffix = imageSuffix;
		this.hasImage = hasImage;
		this.query = query;
		this.options = options;
		this.answerIndex = answerIndex;
		this.imageDocument = imageDocument;
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	public Paper getPaper() {
		return paper;
	}

	public void setPaper(Paper paper) {
		this.paper = paper;
	}

	public String getImageSuffix() {
		return imageSuffix;
	}

	public void setImageSuffix(String imageSuffix) {
		this.imageSuffix = imageSuffix;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public Byte getAnswerIndex() {
		return answerIndex;
	}

	public void setAnswerIndex(Byte answerIndex) {
		this.answerIndex = answerIndex;
	}

	public MultipartFile getImageDocument() {
		return imageDocument;
	}

	public void setImageDocument(MultipartFile imageDocument) {
		this.imageDocument = imageDocument;
	}

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

	public static QuestionBuilder builder() {
		return new QuestionBuilder();
	}

	public static class QuestionBuilder {

		private Id id;
		private Paper paper;
		private String imageSuffix;
		private boolean hasImage;
		private String query;
		private List<String> options;
		private Byte answerIndex;
		private MultipartFile imageDocument;

		public QuestionBuilder() {
			Question question = new Question();
			this.id = question.id;
			this.paper = question.paper;
			this.imageSuffix = question.imageSuffix;
			this.hasImage = question.hasImage;
			this.query = question.query;
			this.options = question.options;
		}

		public QuestionBuilder id(Id id) {
			this.id = id;
			return this;
		}

		public QuestionBuilder paper(Paper paper) {
			this.paper = paper;
			return this;
		}

		public QuestionBuilder imageSuffix(String imageSuffix) {
			this.imageSuffix = imageSuffix;
			return this;
		}

		public QuestionBuilder hasImage(boolean hasImage) {
			this.hasImage = hasImage;
			return this;
		}

		public QuestionBuilder query(String query) {
			this.query = query;
			return this;
		}

		public QuestionBuilder options(List<String> options) {
			this.options = options;
			return this;
		}

		public QuestionBuilder answerIndex(Byte answerIndex) {
			this.answerIndex = answerIndex;
			return this;
		}

		public QuestionBuilder imageDocument(MultipartFile imageDocument) {
			this.imageDocument = imageDocument;
			return this;
		}

		public Question build() {
			return new Question(id, paper, imageSuffix, hasImage, query, options, answerIndex, imageDocument);
		}
	}

	@Override
	public int compareTo(@NotNull Question o) {
		return Integer.compare(this.id.number, o.id.number);
	}

	@Embeddable
	public static class Id {

		private Paper.Id paperId;

		@Column(name = "QUESTION_NUMBER")
		private Integer number;

		public Id() {
		}

		public Id(Paper.Id paperId, Integer number) {
			this.paperId = paperId;
			this.number = number;
		}

		public Paper.Id getPaperId() {
			return paperId;
		}

		public void setPaperId(Paper.Id paperId) {
			this.paperId = paperId;
		}

		public Integer getNumber() {
			return number;
		}

		public void setNumber(Integer number) {
			this.number = number;
		}

		public String createImageSaveName() {
			return paperId.getExamId() +
					paperId.getName() +
					number;
		}

		public static IdBuilder builder() {
			return new IdBuilder();
		}

		public static class IdBuilder {

			private Paper.Id paperId;
			private Integer number;

			public IdBuilder() {
				Id id = new Id();
				this.paperId = id.paperId;
				this.number = id.number;
			}

			public IdBuilder paperId(Paper.Id id) {
				paperId = id;
				return this;
			}

			public IdBuilder number(Integer number) {
				this.number = number;
				return this;
			}

			public Id build() {
				return new Id(paperId, number);
			}
		}
	}
}
