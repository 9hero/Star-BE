package com.mercury.star_be.studygroup.dto.response;

import com.mercury.star_be.studygroup.entity.ConnectionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MemberStatusSseResponse {

	private Long userId;
	private ConnectionStatus status;
}
