package com.mercury.star_be.studygroup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupUpdateResponse {

	private Long id;
	private String name;
	private String image;
	private String description;
	private int maxCapacity;
	private int memberCount;
	private Boolean hasPassword;
	private String password;
	private Boolean isPublic;
}
