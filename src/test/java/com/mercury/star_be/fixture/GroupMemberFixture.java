package com.mercury.star_be.fixture;

import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.user.entity.User;
import java.time.LocalDateTime;

public class GroupMemberFixture {

    public static GroupMember createGroupMember(User user, StudyGroup studyGroup) {
        return GroupMember.builder()
            .member(user)
            .group(studyGroup)
            .isHost(false)
            .joinedAt(LocalDateTime.now())
            .build();
    }

}
