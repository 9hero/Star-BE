package com.mercury.star_be.studygroup.dto.response;

import com.mercury.star_be.studygroup.entity.ConnectionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GroupMemberSseResponse {

	private Long id;
	private String nickname;
	private String image;
	private Boolean isHost;
	private ConnectionStatus status;
}
