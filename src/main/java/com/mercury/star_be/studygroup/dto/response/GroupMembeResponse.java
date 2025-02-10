package com.mercury.star_be.studygroup.dto.response;

import com.mercury.star_be.studygroup.entity.GroupMember;
import lombok.Getter;

@Getter
public class GroupMembeResponse {

    private final Long id;
    private final String nickname;
    private final boolean isHost;
    private final Long memberId;  // member_id를 Long 타입으로 추가

    public GroupMembeResponse(GroupMember groupMember) {
        this.id = groupMember.getId();
        this.nickname = groupMember.getNickname();
        this.isHost = groupMember.isHost();
        this.memberId = groupMember.getMember().getId();  // User 객체의 id를 가져옵니다.
    }

}

