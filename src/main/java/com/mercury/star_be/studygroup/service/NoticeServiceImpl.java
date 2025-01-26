package com.mercury.star_be.studygroup.service;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.NoticeErrorCode;
import com.mercury.star_be.global.error.code.StudyGroupErrorCode;
import com.mercury.star_be.studygroup.dto.request.NoticeCreateRequest;
import com.mercury.star_be.studygroup.dto.request.NoticeUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.NoticeCreateResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeUpdateResponse;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.entity.Notice;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.NoticeRepository;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;



@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Override
    @Transactional
    public NoticeCreateResponse createNotice(NoticeCreateRequest request, Long groupId, Long writerId) {

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

        Notice savedNotice = noticeRepository.save(notice);
        return new NoticeCreateResponse(savedNotice.getId());
    }

    @Override
    @Transactional
    public NoticeUpdateResponse updateNotice(NoticeUpdateRequest request, Long groupId, Long writerId, Long noticeId) {

        GroupMember hostMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, writerId)
                .orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));
        if (!hostMember.isHost()) {
            throw new BusinessException(StudyGroupErrorCode.USER_NOT_HOST);
        }
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(()->new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));

        if (!notice.getStudyGroup().getId().equals(groupId)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_IN_GROUP);
        }

        notice.updateNotice(request.getTitle(), request.getContent());


        //이거 작성자랑 스터디 그룹 굳이 보내줘야하는가? 답) 보내주면 ㅈ된다....
        return NoticeUpdateResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createAt(notice.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteNotice(Long groupId, Long writerId, Long noticeId) {
        GroupMember hostMember = groupMemberRepository.findByGroupIdAndMemberId(groupId, writerId)
                .orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));
        if (!hostMember.isHost()) {
            throw new BusinessException(StudyGroupErrorCode.USER_NOT_HOST);
        }

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(()->new BusinessException(NoticeErrorCode.NOTICE_NOT_FOUND));
        if (!notice.getStudyGroup().getId().equals(groupId)) {
            throw new BusinessException(NoticeErrorCode.NOTICE_NOT_IN_GROUP);
        }
        noticeRepository.delete(notice);

        log.info("공지사항 삭제됨: noticeId={}, groupId={}, deletedBy={}", noticeId, groupId, writerId);
    }

}
