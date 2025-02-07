package com.mercury.star_be.studygroup.entity;

import com.mercury.star_be.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
public class GroupMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 20)
	private String nickname;
	private String image;
	private boolean isHost;
	private LocalDateTime joinedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private StudyGroup group;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private User member;

	@Builder
	public GroupMember( String nickname,  String image,  boolean isHost, StudyGroup group ,User member, LocalDateTime joinedAt) {
		this.nickname = nickname;
		this.image = image;
		this.isHost = isHost;
		this.group = group;
		this.member = member;
		this.joinedAt = joinedAt;

	}

	// TODO: 질문 transactional 을 붙이면 save를 안쳐도 되는것인가?
	//	@Transactional
	public void updateGroupMember(Long id, String nickname, String image, boolean isHost, StudyGroup group , User member, LocalDateTime joinedAt) {
		this.id = id;
		this.nickname = nickname;
		this.image = image;
		this.isHost = isHost;
		this.group = group;
		this.member = member;
		this.joinedAt = joinedAt;

	}

	public String changeNickname(String nickname) {
		this.nickname = nickname;
		return this.nickname;
	}

	public void setHost() {
		this.isHost = true;
	}
}
