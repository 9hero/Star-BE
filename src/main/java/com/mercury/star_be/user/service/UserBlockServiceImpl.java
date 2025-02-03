package com.mercury.star_be.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.UserErrorCode;
import com.mercury.star_be.user.dto.request.UserBlockRequest;
import com.mercury.star_be.user.entity.BlockUser;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.BlockUserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {

	private final UserService userService;
	private final BlockUserRepository blockUserRepository;

	@Override
	@Transactional
	public void blockUser(Long userId, UserBlockRequest userBlockRequest) {
		Long targetUserId = userBlockRequest.getTargetUserId();
		userService.findById(targetUserId);
		if (isBlockUser(userId, targetUserId)) {
			throw new BusinessException(UserErrorCode.ALREADY_BLOCK_USER);
		}

		User user = userService.findById(userId);
		BlockUser blockUser = BlockUser.builder()
			.blockUserId(targetUserId)
			.createdAt(LocalDateTime.now())
			.user(user)
			.build();
		user.addBlockUser(blockUser);
	}

	@Override
	public boolean isBlockUser(Long userId, Long targetUserId) {
		return blockUserRepository.existsByUserIdAndBlockUserId(userId, targetUserId);
	}
}
