package com.polarbookshop.edgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.*;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository) throws Exception {

        CookieServerCsrfTokenRepository tokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
        XorServerCsrfTokenRequestAttributeHandler delegate = new XorServerCsrfTokenRequestAttributeHandler();
        ServerCsrfTokenRequestHandler requestHandler = delegate::handle;

        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
                        .pathMatchers(HttpMethod.GET, "/books/**").permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository )
                        .csrfTokenRequestHandler(requestHandler))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
                .build();
    }

    // 웹 세션에 엑세스 토큰을 저장하기 위한 repository 정의
    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);

        // 키클록에서 로그아웃 후 사용자를 스프링에서 지정하는 베이스 URL로 리다이렉트
        oidcLogoutSuccessHandler
                .setPostLogoutRedirectUri("{baseUrl}");

        return oidcLogoutSuccessHandler;
    }

    // CsrfToken 리액티브 스트림을 subscribe하고 토큰 값을 추출하기 위한 필터
    @Bean
    WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty());
            return csrfToken.doOnSuccess(token -> {
                /* Ensures the token is subscribed to. */
            }).then(chain.filter(exchange));
        };
    }

//    @Bean
//    WebFilter csrfWebFilter() {
//        return (exchange, chain) -> {
//            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
//                Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
//                return csrfToken != null ? csrfToken.then() : Mono.empty();
//            }));
//            return chain.filter(exchange);
//        };
//    }

}