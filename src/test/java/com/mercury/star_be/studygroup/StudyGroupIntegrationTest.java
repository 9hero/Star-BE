package com.mercury.star_be.studygroup;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;

@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudyGroupIntegrationTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;

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
			.andDo(print());
	}
}
