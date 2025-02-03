package com.mercury.star_be.user.service;

import com.mercury.star_be.user.dto.request.UserBlockRequest;

public interface UserBlockService {

	void blockUser(Long userId, UserBlockRequest userBlockRequest);

	boolean isBlockUser(Long userId, Long targetUserId);
}
