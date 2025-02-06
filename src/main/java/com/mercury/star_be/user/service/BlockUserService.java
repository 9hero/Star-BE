package com.mercury.star_be.user.service;

import java.util.List;

import com.mercury.star_be.user.dto.request.UserBlockRequest;
import com.mercury.star_be.user.dto.request.UserUnblockRequest;
import com.mercury.star_be.user.dto.response.BlockUserListResponse;

public interface BlockUserService {

	void blockUser(Long userId, UserBlockRequest userBlockRequest);

	void unblockUser(Long userId, UserUnblockRequest userUnblockRequest);

	boolean isBlockUser(Long userId, Long targetUserId);

	List<BlockUserListResponse> getBlockUserList(Long userId);
}
