package com.mercury.star_be.user.repository;

import static com.mercury.star_be.user.entity.QBlockUser.*;
import static com.mercury.star_be.user.entity.QUser.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mercury.star_be.user.dto.response.BlockUserListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BlockUserCustomRepositoryImpl implements BlockUserCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<BlockUserListResponse> findAllByUserId(Long userId) {
		return jpaQueryFactory.select(
			Projections.constructor(BlockUserListResponse.class,
				blockUser.blockUserId,
				user.nickname,
				user.image
			)
		)
			.from(blockUser)
			.leftJoin(user).on(blockUser.blockUserId.eq(user.id))
			.where(blockUser.user.id.eq(userId))
			.orderBy(blockUser.createdAt.desc())
			.fetch();
	}
}
