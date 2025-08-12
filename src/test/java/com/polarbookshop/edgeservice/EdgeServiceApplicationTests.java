package com.polarbookshop.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers // 테스트컨테이너의 시작과 종료 자동화
class EdgeServiceApplicationTests {

	private static final int REDIS_PORT = 6379;

	@MockitoBean
	ReactiveClientRegistrationRepository clientRegistrationRepository;

	@Test
	void contextLoads() {
	}

	@Container
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
			.withExposedPorts(REDIS_PORT);

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redis::getHost);
		registry.add("spring.redis.port", () -> redis.getMappedPort(REDIS_PORT));
	}
}
