package com.mercury.star_be.studygroup.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupCreateRequest {

	@NotBlank
	@Size(min = 2, max = 50, message = "그룹명은 2자 이상 50자 이하 이어야 합니다.")
	private String name;
	@NotBlank
	@Size(min = 2, max = 500, message = "그룹 설명은 2자 이상 500자 이하 이어야 합니다.")
	private String description;
	@NotBlank
	private String image;
	@Min(value = 2, message = "최대인원은 2이상 이어야 합니다.")
	private int maxCapacity;
	private boolean hasPassword;
	@Size(max = 50, message = "비밀번호는 50자 이하 이어야 합니다.")
	private String password;
	private boolean isPublic;

	public boolean hasPassword() {
		return hasPassword;
	}
}
