package com.mercury.star_be.user.service;

import com.mercury.star_be.user.dto.request.UserBlockRequest;
import com.mercury.star_be.user.dto.request.UserUnblockRequest;

public interface UserBlockService {

	void blockUser(Long userId, UserBlockRequest userBlockRequest);

	void unblockUser(Long userId, UserUnblockRequest userUnblockRequest);

	boolean isBlockUser(Long userId, Long targetUserId);
}
