package com.mercury.star_be.user.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.mercury.star_be.chat.entity.ChatMessage;
import com.mercury.star_be.chat.entity.UserChatRoom;
import com.mercury.star_be.studygroup.entity.GroupMember;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	@Column(length = 20)
	private String nickname;
	@Column(length = 10)
	private String provider;
	private String image;
	private boolean isActive;
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GroupMember> groupMembers;

	@Builder
	public User(String email, String nickname, String provider, String image) {
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.image = image;
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
	}

	@OneToMany(mappedBy = "chatUser", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserChatRoom> chatRoomLists;

	@OneToMany(mappedBy = "chatSender", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMessage> sentMessages;

	@OneToMany(mappedBy = "chatReceiver", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMessage> receivedMessages;


}
