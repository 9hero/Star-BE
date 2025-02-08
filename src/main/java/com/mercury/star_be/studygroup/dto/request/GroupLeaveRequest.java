package com.mercury.star_be.studygroup.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GroupLeaveRequest {


    private List<GroupMemberInfo> groupMemberInfos;

    @Data
    public static class GroupMemberInfo {
        private Long groupId;
        private Long memberId;
    }
}
