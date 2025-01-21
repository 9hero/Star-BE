package com.mercury.star_be.studygroup;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.mercury.star_be.common.RestDocsTestSupport;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;

class StudyGroupIntegrationTest extends RestDocsTestSupport {

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
}
