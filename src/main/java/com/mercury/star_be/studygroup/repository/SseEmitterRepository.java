package com.mercury.star_be.studygroup.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SseEmitterRepository {

	private final Map<Long, Set<SseEmitter>> sseEmittersMap = new ConcurrentHashMap<>();
	private final RedisTemplate<String, Object> redisTemplate;

	private static final String GROUP_PREFIX = "study_group";

	public void save(Long groupId, Long userId, SseEmitter sseEmitter) {
		sseEmittersMap.computeIfAbsent(groupId, l -> ConcurrentHashMap.newKeySet()).add(sseEmitter);
		redisTemplate.opsForSet().add(GROUP_PREFIX + groupId, userId.toString());
	}

	public void delete(Long groupId, Long userId, SseEmitter sseEmitter) {
		Set<SseEmitter> sseEmitters = sseEmittersMap.getOrDefault(groupId, Collections.emptySet());
		sseEmitters.remove(sseEmitter);
		if (sseEmitters.isEmpty()) sseEmittersMap.remove(groupId);

		redisTemplate.opsForSet().remove(GROUP_PREFIX + groupId, userId.toString());
	}

	public List<Long> getConnectedUsers(Long groupId) {
		return redisTemplate.opsForSet().members(GROUP_PREFIX + groupId).stream()
			.map(o -> Long.valueOf(o.toString()))
			.toList();
	}

	public Set<SseEmitter> findAllByGroupId(Long groupId) {
		return sseEmittersMap.getOrDefault(groupId, Collections.emptySet());
	}
}
