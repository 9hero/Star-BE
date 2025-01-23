package com.mercury.star_be.chat.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**채팅방 타입*/
@Getter
@RequiredArgsConstructor
public enum ChatRoomType {
    GROUP,
    DM
}
