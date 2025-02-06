package com.mercury.star_be.studygroup.service;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.NoticeErrorCode;
import com.mercury.star_be.global.error.code.StudyGroupErrorCode;
import com.mercury.star_be.studygroup.dto.request.NoticeCreateRequest;
import com.mercury.star_be.studygroup.dto.request.NoticeUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.NoticeCreateResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeUpdateResponse;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.entity.Notice;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.NoticeRepository;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import com.mercury.star_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JwtUtil jwtUtil;
    @Override
    @Transactional
    public NoticeCreateResponse createNotice(NoticeCreateRequest request, Long groupId, String token) {
        Long writerId = jwtUtil.getid(token);
        User user = userRepository.findById(writerId).orElseThrow();
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
        GroupMember hostMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, writerId )
                .orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));

        if (!hostMember.isHost()) {
            throw new BusinessException(StudyGroupErrorCode.USER_NOT_HOST);
        }
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(user)
                .studyGroup(studyGroup)
                .createdAt(LocalDateTime.now())
                .build();
        studyGroup.addNotice(notice);
        Notice savedNotice = noticeRepository.save(notice);
        NoticeCreateResponse response = NoticeCreateResponse.builder()
                .writer(savedNotice.getWriter().getNickname())
                .createdAt(savedNotice.getCreatedAt())
                .title(savedNotice.getTitle())
                .content(savedNotice.getTitle())
                .id(savedNotice.getId())
                .build();
        return response;
    }

    @Override
    @Transactional
    public NoticeUpdateResponse updateNotice(NoticeUpdateRequest request, Long groupId, String token, Long noticeId) {
        Long writerId = jwtUtil.getid(token);
        GroupMember hostMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, writerId)
                .orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));
        if (!hostMember.isHost()) {
            throw new BusinessException(StudyGroupErrorCode.USER_NOT_HOST);
        }
        Notice notice = findById(noticeId);

        if (!notice.getStudyGroup().getId().equals(groupId)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_IN_GROUP);
        }

        notice.updateNotice(request.getTitle(), request.getContent());
        System.out.println(notice.getCreatedAt());

        return NoticeUpdateResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .writer(notice.getWriter().getNickname())
                .build();
    }

    @Override
    @Transactional
    public void deleteNotice(Long groupId, String token, Long noticeId) {
        Long writerId = jwtUtil.getid(token);
        GroupMember hostMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, writerId)
                .orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));
        if (!hostMember.isHost()) {
            throw new BusinessException(StudyGroupErrorCode.USER_NOT_HOST);
        }

        Notice notice = findById(noticeId);
        if (!notice.getStudyGroup().getId().equals(groupId)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_IN_GROUP);
        }
        noticeRepository.delete(notice);

        log.info("공지사항 삭제됨: noticeId={}, groupId={}, deletedBy={}", noticeId, groupId, writerId);
    }

	@Override
	public List<NoticeResponse> getNoticeList(Long groupId) {
        return noticeRepository.findAllByGroupId(groupId);
    }

    @Override
    public NoticeResponse getNotice(Long groupId, Long noticeId) {
        return noticeRepository.findByGroupIdAndNoticeId(groupId, noticeId);
    }

    private Notice findById(Long noticeId) {
        return noticeRepository.findById(noticeId)
            .orElseThrow(() -> new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

}
