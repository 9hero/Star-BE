package com.mercury.star_be.user.repository;

import java.util.List;

import com.mercury.star_be.user.dto.response.BlockUserListResponse;

public interface BlockUserCustomRepository {

	List<BlockUserListResponse> findAllByUserId(Long userId);
}
