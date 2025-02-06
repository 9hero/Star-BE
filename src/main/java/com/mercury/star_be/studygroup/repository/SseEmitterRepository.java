package com.mercury.star_be.studygroup.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mercury.star_be.studygroup.entity.ConnectionStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SseEmitterRepository {

	private final Map<Long, Set<SseEmitter>> sseEmittersMap = new ConcurrentHashMap<>();
	private final RedisTemplate<String, Object> redisTemplate;

	private static final String GROUP_PREFIX = "study_group";

	public void save(Long groupId, SseEmitter sseEmitter) {
		sseEmittersMap.computeIfAbsent(groupId, l -> new CopyOnWriteArraySet<>()).add(sseEmitter);
	}

	public void updateStatus(Long groupId, Long userId, ConnectionStatus status) {
		redisTemplate.opsForHash().put(GROUP_PREFIX + groupId, userId.toString(), status.toString());
	}

	public void delete(Long groupId, Long userId, SseEmitter sseEmitter) {
		Set<SseEmitter> sseEmitters = sseEmittersMap.get(groupId);
		if (sseEmitters != null) {
			sseEmitters.remove(sseEmitter);
			if (sseEmitters.isEmpty()) sseEmittersMap.remove(groupId);
		}

		redisTemplate.opsForHash().delete(GROUP_PREFIX + groupId, userId.toString());
	}

	public Map<Object, Object> getConnectedUsers(Long groupId) {
		return redisTemplate.opsForHash().entries(GROUP_PREFIX + groupId);
	}

	public Set<SseEmitter> findAllByGroupId(Long groupId) {
		return sseEmittersMap.getOrDefault(groupId, Collections.emptySet());
	}

	@Scheduled(fixedRate = 30 * 1000)	// 30초
	public void sendHeartbeat() {
		for (Map.Entry<Long, Set<SseEmitter>> entry : sseEmittersMap.entrySet()) {
			Set<SseEmitter> emitters = entry.getValue();
			for (SseEmitter emitter : emitters) {
				try {
					emitter.send(SseEmitter.event()
						.name("heartbeat")
						.data("ping"));
				} catch (IOException e) {
					emitter.complete();
				}
			}
		}
	}
}
