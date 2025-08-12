package com.polarbookshop.edgeservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> exchange.getPrincipal() // 현재 인증된 사용자를 현재 요청(exchange)에서 가져온다
                .map(Principal::getName) // principal에서 유저이름 추출
                .defaultIfEmpty("anonymous"); // 요청이 인증되지 않았다면 상수 사용
    }
}
