package com.mercury.star_be.studygroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GroupMemberDto {

	private Long id;
	private String nickname;
	private String image;
	private Boolean isHost;
	private Long studyTime;
	private Long groupId;

	public boolean isHost() {
		return this.isHost;
	}
}
