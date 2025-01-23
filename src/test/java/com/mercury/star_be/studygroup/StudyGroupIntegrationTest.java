package com.mercury.star_be.studygroup;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mercury.star_be.fixture.StudyGroupFixture;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.mercury.star_be.common.RestDocsTestSupport;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

class StudyGroupIntegrationTest extends RestDocsTestSupport {

	@Autowired
	StudyGroupRepository studyGroupRepository;

	@Test
	@DisplayName("스터디 그룹을 생성한다.")
	void createStudyGroup() throws Exception {
	    // given
		StudyGroupCreateRequest studyGroupCreateRequest = StudyGroupCreateRequest.builder()
			.name("테스트 그룹")
			.description("테스트 그룹 설명")
			.image("이미지")
			.maxCapacity(10)
			.isPublic(true)
			.hasPassword(true)
			.password("1234")
			.build();
		String content = objectMapper.writeValueAsString(studyGroupCreateRequest);

		// when & then
		mockMvc.perform(post("/api/groups")
				.content(content)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(1))
			.andDo(restDocs.document());
	}

	@Test
	@DisplayName("스터디 그룹 정보를 수정한다.")
	void updateStudyGroup() throws Exception {
	    // given
		createStudyGroup();
		StudyGroupUpdateRequest request = StudyGroupUpdateRequest.builder()
			.name("수정한 그룹명")
			.description("수정한 그룹 설명")
			.image("수정한 이미지")
			.maxCapacity(15)
			.isPublic(false)
			.hasPassword(false)
			.password("")
			.build();
		String content = objectMapper.writeValueAsString(request);

		// when & then
		mockMvc.perform(put("/api/groups/{groupId}", 1L)
			.content(content)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("수정한 그룹명"))
			.andExpect(jsonPath("$.data.description").value("수정한 그룹 설명"))
			.andExpect(jsonPath("$.data.image").value("수정한 이미지"))
			.andExpect(jsonPath("$.data.isPublic").value(false))
			.andDo(restDocs.document());
	}

	@Test
	@DisplayName("그룹ID로 스터디 그룹 정보를 조회한다.")
	void getStudyGroup() throws Exception {
	    // given
		createStudyGroup();
		Long groupId = 1L;

	    // when & then
	    mockMvc.perform(get("/api/groups/{groupId}", groupId)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(1))
			.andExpect(jsonPath("$.data.name").value("테스트 그룹"))
			.andExpect(jsonPath("$.data.description").value("테스트 그룹 설명"))
			.andExpect(jsonPath("$.data.image").value("이미지"))
			.andExpect(jsonPath("$.data.maxCapacity").value(10))
			.andExpect(jsonPath("$.data.memberCount").value(1))
			.andExpect(jsonPath("$.data.isPublic").value(true))
			.andExpect(jsonPath("$.data.hasPassword").value(true))
			.andExpect(jsonPath("$.data.password").value("1234"))
			.andDo(restDocs.document());
	}

	void setUp() {
		studyGroupRepository.deleteAll();
		studyGroupRepository.saveAll(List.of(
				StudyGroupFixture.createStudyGroup(
						"그룹1", "토익공부", "이미지", 10,
						true, false, "", 8,
						LocalDateTime.parse("2025-01-22T16:09:05.55356")
				),
		StudyGroupFixture.createStudyGroup(
				"그룹2", "사회", "이미지", 10,
				false, false, "", 7,
				LocalDateTime.parse("2025-01-21T16:09:05.55356")
		),
		StudyGroupFixture.createStudyGroup(
				"그룹3", "체육공부", "이미지", 10,
				true, false, "", 5,
				LocalDateTime.parse("2025-01-20T16:09:05.55356")
		),
		StudyGroupFixture.createStudyGroup(
				"그룹4", "과학공부", "이미지", 10,
				true, false, "", 3,
				LocalDateTime.parse("2025-01-19T16:09:05.55356")
		),
		StudyGroupFixture.createStudyGroup(
				"그룹5", "수학공부", "이미지", 10,
				true, false, "", 2,
				LocalDateTime.parse("2025-01-18T16:09:05.55356")
		),
		StudyGroupFixture.createStudyGroup(
				"그룹6", "토익공부", "이미지", 10,
				true, false, "", 6,
				LocalDateTime.parse("2025-01-17T16:09:05.55356")
		)
		)
		);

	}

	@Test
	@DisplayName("스터디 그룹 리스트를 조회한다 - 키워드 없는 경우")
	void getStudyGroupList() throws Exception {
		setUp();

		mockMvc.perform(get("/api/groups")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].name").value("그룹1")) // 최신 그룹
				.andExpect(jsonPath("$.data.content[0].memberCount").value(8))
				.andExpect(jsonPath("$.data.content[1].name").value("그룹3"))
				.andExpect(jsonPath("$.data.content[1].memberCount").value(5))
				.andExpect(jsonPath("$.data.content[2].name").value("그룹4"))
				.andExpect(jsonPath("$.data.content[2].memberCount").value(3))
				.andExpect(jsonPath("$.data.content[3].name").value("그룹5"))
				.andExpect(jsonPath("$.data.content[3].memberCount").value(2))
				.andExpect(jsonPath("$.data.content[4].name").value("그룹6"))
				.andExpect(jsonPath("$.data.content[4].memberCount").value(6))
				.andExpect(jsonPath("$.data.currentPage").value(0))
				.andExpect(jsonPath("$.data.last").value(true))
				.andDo(restDocs.document());
	}

	@Test
	@DisplayName("스터디 그룹 리스트를 조회한다 - 키워드가 '토익'이고 인원 많은 순")
	void getStudyGroupList_keywordAndSortByMemberCount() throws Exception {
		setUp();

		// when & then
		mockMvc.perform(get("/api/groups")
						.param("keyword", "토익")
						.param("sort", "memberCount")
						.param("direction", "desc")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andDo(print()) // 응답 출력
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].name").value("그룹1")) // 토익 & 인원이 가장 많은 그룹
				.andExpect(jsonPath("$.data.content[0].memberCount").value(8))
				.andExpect(jsonPath("$.data.content[0].isPublic").value(true))
				.andExpect(jsonPath("$.data.content[1].name").value("그룹6")) // 두 번째로 많은 토익 그룹
				.andExpect(jsonPath("$.data.content[1].memberCount").value(6))
				.andExpect(jsonPath("$.data.content[1].isPublic").value(true))
				.andExpect(jsonPath("$.data.content.length()").value(2)) // 반환된 그룹 수는 2개
				.andExpect(jsonPath("$.data.currentPage").value(0))
				.andExpect(jsonPath("$.data.last").value(true))
				.andDo(restDocs.document());
	}

	@Test
	@DisplayName("스터디 그룹 리스트를 조회한다 - 키워드가 '그룹'이고 생성일 기준 최신순")
	void getStudyGroupList_keywordAndSortByCreatedAt() throws Exception {
		setUp();

		// when & then
		mockMvc.perform(get("/api/groups")
						.param("keyword", "그룹")
						.param("sort", "createdAt")
						.param("direction", "asc")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].name").value("그룹6")) // 가장 오래된 그룹
				.andExpect(jsonPath("$.data.content[0].createdAt").value("2025-01-17T16:09:05.55356"))
				.andExpect(jsonPath("$.data.content[0].isPublic").value(true))
				.andExpect(jsonPath("$.data.content[1].name").value("그룹5")) // 두 번째 오래된 그룹
				.andExpect(jsonPath("$.data.content[1].createdAt").value("2025-01-18T16:09:05.55356"))
				.andExpect(jsonPath("$.data.content[1].isPublic").value(true))
				.andExpect(jsonPath("$.data.content[2].name").value("그룹4")) // 세 번째 오래된 그룹
				.andExpect(jsonPath("$.data.content[2].createdAt").value("2025-01-19T16:09:05.55356"))
				.andExpect(jsonPath("$.data.content[2].isPublic").value(true))
				.andExpect(jsonPath("$.data.content[3].name").value("그룹3")) // 네 번째 오래된 그룹
				.andExpect(jsonPath("$.data.content[3].createdAt").value("2025-01-20T16:09:05.55356"))
				.andExpect(jsonPath("$.data.content[3].isPublic").value(true))
				.andExpect(jsonPath("$.data.content[4].name").value("그룹1")) // 최신 그룹
				.andExpect(jsonPath("$.data.content[4].createdAt").value("2025-01-22T16:09:05.55356"))
				.andExpect(jsonPath("$.data.content[4].isPublic").value(true))
				.andExpect(jsonPath("$.data.content.length()").value(5)) // 반환된 그룹 수는 5개
				.andExpect(jsonPath("$.data.currentPage").value(0))
				.andExpect(jsonPath("$.data.last").value(true))
				.andDo(restDocs.document());
	}

}
