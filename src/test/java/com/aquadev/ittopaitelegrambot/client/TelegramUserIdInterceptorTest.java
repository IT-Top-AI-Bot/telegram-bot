package com.aquadev.ittopaitelegrambot.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TelegramUserIdInterceptorTest {

    @Mock
    ClientHttpRequestExecution execution;
    @Mock
    ClientHttpResponse response;

    TelegramUserIdInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new TelegramUserIdInterceptor();
    }

    @Test
    void intercept_withScopedValue_addsHeader() throws Exception {
        given(execution.execute(any(), any())).willReturn(response);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));

        ScopedValue.where(TelegramUserContext.TG_USER_ID, 12345L)
                .call(() -> interceptor.intercept(request, new byte[0], execution));

        assertThat(request.getHeaders().getFirst("X-Telegram-User-Id")).isEqualTo("12345");
    }

    @Test
    void intercept_withoutScopedValue_throwsIllegalState() {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], execution))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X-Telegram-User-Id");
    }
}
