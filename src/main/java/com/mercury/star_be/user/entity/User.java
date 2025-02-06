package com.mercury.star_be.user.entity;

import com.mercury.star_be.chat.entity.ChatMessage;
import com.mercury.star_be.chat.entity.UserChatRoom;
import com.mercury.star_be.studygroup.entity.GroupMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	@Column(length = 10)
	@Setter
	@Getter
	private String nickname;
	@Column(length = 10)
	private String provider;
	@Column(name = "oauthId", length = 50)
	private String oauthId;
	@Setter
	private String image;
	@Setter
	@Getter
	private boolean isActive;
	private LocalDateTime createdAt;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private RefreshToken refreshTokens;

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GroupMember> groupMembers;

	@Builder
	public User(String email, String nickname, String provider, String image, String oauthId) {
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.image = image;
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
		this.oauthId = oauthId;
	}

	@OneToMany(mappedBy = "chatUser", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserChatRoom> chatRoomLists;

	@OneToMany(mappedBy = "chatSender", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMessage> sentMessages;

	@OneToMany(mappedBy = "chatReceiver", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ChatMessage> receivedMessages;


}
