package com.aquadev.ittopaitelegrambot.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BackendLoggingInterceptorTest {

    @Mock
    ClientHttpRequestExecution execution;
    @Mock
    ClientHttpResponse response;

    BackendLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() throws IOException {
        interceptor = new BackendLoggingInterceptor();
        given(execution.execute(any(), any())).willReturn(response);
    }

    @Test
    void loggableHeader_contentType_isPassedThrough() throws IOException {
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api/test"));
        request.getHeaders().add("content-type", "application/json");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        try (var res = interceptor.intercept(request, new byte[0], execution)) {
            try (var ignored = verify(execution).execute(captor.capture(), any())) {
            }
            assertThat(captor.getValue().getHeaders().getFirst("content-type")).isEqualTo("application/json");
            assertThat(res).isSameAs(response);
        }
    }

    @Test
    void nonLoggableHeader_authorization_isMasked() throws IOException {
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("/api"));
        request.getHeaders().add("Authorization", "Bearer secret-token");
        request.getHeaders().add("content-type", "application/json");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        try (var res = interceptor.intercept(request, new byte[0], execution)) {
            try (var ignored = verify(execution).execute(captor.capture(), any())) {
            }
            assertThat(captor.getValue().getHeaders().getFirst("Authorization")).isEqualTo("***");
            assertThat(captor.getValue().getHeaders().getFirst("content-type")).isEqualTo("application/json");
            assertThat(res).isSameAs(response);
        }
    }

    @Test
    void response2xx_doesNotThrow() throws IOException {
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));

        try (ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution)) {
            assertThat(result).isSameAs(response);
        }
    }

    @Test
    void response4xx_doesNotThrow() throws IOException {
        given(response.getStatusCode()).willReturn(HttpStatus.NOT_FOUND);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));

        try (ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution)) {
            assertThat(result).isSameAs(response);
        }
    }

    @Test
    void response5xx_doesNotThrow() throws IOException {
        given(response.getStatusCode()).willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));

        try (ClientHttpResponse result = interceptor.intercept(request, new byte[0], execution)) {
            assertThat(result).isSameAs(response);
        }
    }

    @Test
    void xTelegramUserIdHeader_isLoggable_notMasked() throws IOException {
        given(response.getStatusCode()).willReturn(HttpStatus.OK);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("/api"));
        request.getHeaders().add("X-Telegram-User-Id", "12345");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        try (var res = interceptor.intercept(request, new byte[0], execution)) {
            try (var ignored = verify(execution).execute(captor.capture(), any())) {
            }
            assertThat(captor.getValue().getHeaders().getFirst("X-Telegram-User-Id")).isEqualTo("12345");
            assertThat(res).isSameAs(response);
        }
    }
}
