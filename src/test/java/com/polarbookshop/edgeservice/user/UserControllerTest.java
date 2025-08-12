package com.polarbookshop.edgeservice.user;

import com.polarbookshop.edgeservice.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    WebTestClient webClient;

    // 클라이언트 등록에 대한 정보를 가져올 때 모의 빈
    @MockitoBean
    ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void whenNotAuthenticatedThen401() {
        webClient
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedThenReturnUser() {
        User expectedUser = new User("jon.snow", "Jon",
                "Snow", List.of("employee", "customer"));

        webClient
                .mutateWith(configureMockOidcLogin(expectedUser)) // OIDC기반 인증 context 정의
                .get()
                .uri("/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(User.class)
                .value(user -> assertThat(user).isEqualTo(expectedUser));

    }

    private SecurityMockServerConfigurers.OidcLoginMutator configureMockOidcLogin(User expectedUser) {
        return SecurityMockServerConfigurers.mockOidcLogin().idToken(
                builder -> { // mock id 토큰 생성
                    builder.claim(StandardClaimNames.PREFERRED_USERNAME, expectedUser.username());
                    builder.claim(StandardClaimNames.GIVEN_NAME, expectedUser.firstName());
                    builder.claim(StandardClaimNames.FAMILY_NAME, expectedUser.lastName());
                    builder.claim("roles", expectedUser.roles());
                }
        );
    }

}