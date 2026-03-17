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
import static org.mockito.Mockito.verify;

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

        try (var res = ScopedValue.where(TelegramUserContext.TG_USER_ID, 12345L)
                .call(() -> interceptor.intercept(request, new byte[0], execution))) {
            verify(execution).execute(any(), any());
            assertThat(request.getHeaders().getFirst("X-Telegram-User-Id")).isEqualTo("12345");
            assertThat(res).isSameAs(response);
        }
    }

    @Test
    void intercept_withoutScopedValue_throwsIllegalState() {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));

        assertThatThrownBy(() -> executeIntercept(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X-Telegram-User-Id");
    }

    private void executeIntercept(MockClientHttpRequest request) throws Exception {
        try (var res = interceptor.intercept(request, new byte[0], execution)) {
            assertThat(res).isNotNull();
        }
    }
}
