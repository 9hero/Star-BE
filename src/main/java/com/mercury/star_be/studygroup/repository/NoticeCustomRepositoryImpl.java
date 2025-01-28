package com.mercury.star_be.studygroup.repository;

import static com.mercury.star_be.studygroup.entity.QGroupMember.*;
import static com.mercury.star_be.studygroup.entity.QNotice.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mercury.star_be.studygroup.dto.response.NoticeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NoticeCustomRepositoryImpl implements NoticeCustomRepository {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<NoticeResponse> findAllByGroupId(Long groupId) {
		return jpaQueryFactory.select(
				Projections.constructor(NoticeResponse.class,
					notice.id,
					notice.title,
					notice.content,
					groupMember.nickname.as("writer"),
					notice.createdAt
				)
			)
			.from(notice)
			.leftJoin(groupMember).on(notice.writer.id.eq(groupMember.member.id)
				.and(groupMember.group.id.eq(groupId)))
			.where(notice.studyGroup.id.eq(groupId))
			.fetch();
	}

	@Override
	public NoticeResponse findByGroupIdAndNoticeId(Long groupId, Long noticeId) {
		return jpaQueryFactory.select(
				Projections.constructor(NoticeResponse.class,
					notice.id,
					notice.title,
					notice.content,
					groupMember.nickname.as("writer"),
					notice.createdAt
				)
			)
			.from(notice)
			.leftJoin(groupMember).on(notice.writer.id.eq(groupMember.member.id)
				.and(groupMember.group.id.eq(groupId)))
			.where(notice.id.eq(noticeId))
			.fetchOne();
	}
}
