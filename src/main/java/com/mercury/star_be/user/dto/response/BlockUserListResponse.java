package com.mercury.star_be.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BlockUserListResponse {

	private Long id;
	private String nickname;
	private String image;
}
