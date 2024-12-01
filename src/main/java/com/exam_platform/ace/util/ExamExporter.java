package com.exam_platform.ace.util;

import com.exam_platform.ace.entity.Candidate;
import com.exam_platform.ace.entity.Exam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
public class ExamExporter {

	@Data
	@EqualsAndHashCode(callSuper = false)
	@NoArgsConstructor
	public static class ExportConfig extends Exam.CandidateConfig {
		private String usernameDesc;
		private boolean exportAllCandidates = false;
		private String totalDesc = "Total";
		private int cutOffPercentile = 0;
		public ExportConfig(Exam.CandidateConfig base) {
			super(
					base.isEmail(), base.getEmailDesc(),
					base.isPhoneNumber(), base.getPhoneNumberDesc(),
					base.isAddress(), base.getAddressDesc(),
					base.isState(), base.getStateDesc(),
					base.isFirstname(), base.getFirstnameDesc(),
					base.isLastname(), base.getLastnameDesc(),
					base.isOtherNames(), base.getOtherNamesDesc()
			);
		}
		public String getHeader() {
			StringBuilder sb = new StringBuilder();
			sb.append(usernameDesc).append(",");
			if (isEmail())
				sb.append(getEmailDesc()).append(",");
			if (isPhoneNumber())
				sb.append(getPhoneNumberDesc()).append(",");
			if (isLastname())
				sb.append(getLastnameDesc()).append(",");
			if (isFirstname())
				sb.append(getFirstnameDesc()).append(",");
			if (isOtherNames())
				sb.append(getOtherNamesDesc()).append(",");
			if (isAddress())
				sb.append(getAddressDesc()).append(",");
			if (isState())
				sb.append(getStateDesc()).append(",");
			sb.append(totalDesc).append("\n");
			return sb.toString();
		}
	}

	private ExportConfig config;

	public void setConfig(Exam exam) {
		config = new ExportConfig(exam.getCandidateConfig());
		config.setUsernameDesc(exam.getUsernameDesc());
	}

	public String exportToCSV(Exam exam) {
		StringBuilder body = new StringBuilder();
		for (Candidate candidate : exam.getCandidates()) {
			body.append(candidate.getId().getUsername()).append(",");
			if (config.isEmail()) {
				body.append(candidate.getEmail()).append(",");
			}
		}
		return config.getHeader() + body.toString();
	}
}
