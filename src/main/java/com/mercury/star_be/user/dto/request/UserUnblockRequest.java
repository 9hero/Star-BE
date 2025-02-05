package com.mercury.star_be.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUnblockRequest {

	Long targetUserId;
}
