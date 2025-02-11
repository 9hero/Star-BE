package com.mercury.star_be.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.UserErrorCode;
import com.mercury.star_be.user.dto.request.UserBlockRequest;
import com.mercury.star_be.user.dto.request.UserUnblockRequest;
import com.mercury.star_be.user.dto.response.BlockUserListResponse;
import com.mercury.star_be.user.entity.BlockUser;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.BlockUserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BlockUserServiceImpl implements BlockUserService {

	private final UserService userService;
	private final BlockUserRepository blockUserRepository;

	@Override
	@Transactional
	public void blockUser(Long userId, UserBlockRequest userBlockRequest) {
		Long targetUserId = userBlockRequest.getTargetUserId();
		userService.findById(targetUserId);
		if (isBlockUser(userId, targetUserId)) {
			throw new BusinessException(UserErrorCode.ALREADY_BLOCKED_USER);
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
	@Transactional
	public void unblockUser(Long userId, UserUnblockRequest userUnblockRequest) {
		Long targetUserId = userUnblockRequest.getTargetUserId();
		userService.findById(targetUserId);

		BlockUser blockUser = blockUserRepository.findByUserIdAndBlockUserId(userId, targetUserId)
			.orElseThrow(() -> new BusinessException(UserErrorCode.NOT_BLOCKED_USER));

		User user = userService.findById(userId);
		user.unblockUser(blockUser);
	}

	@Override
	@Transactional
	public void unblockUser(Long userId, Long targetUserId) {
		userService.findById(targetUserId);
		BlockUser blockUser = blockUserRepository.findByUserIdAndBlockUserId(userId, targetUserId)
				.orElseThrow(() -> new BusinessException(UserErrorCode.NOT_BLOCKED_USER));
		User user = userService.findById(userId);
		user.unblockUser(blockUser);
	}




	@Override
	@Transactional
	public void unblockUser(Long userId) {
		User user = userService.findById(userId);
		List<BlockUser> BlockUserListResponseDto =  blockUserRepository.findAllBlockUsersByUserId(userId);
		for(BlockUser blockUser : BlockUserListResponseDto) {
			user.unblockUser(blockUser);
		}
	}





	@Override
	public boolean isBlockUser(Long userId, Long targetUserId) {
		return blockUserRepository.existsByUserIdAndBlockUserId(userId, targetUserId);
	}

	@Override
	public List<BlockUserListResponse> getBlockUserList(Long userId) {
		return blockUserRepository.findAllByUserId(userId);
	}
}
