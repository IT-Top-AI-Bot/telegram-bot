package com.aquadev.ittopaitelegrambot.client;

import com.aquadev.ittopaitelegrambot.client.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private MockRestServiceServer server;
    private UserClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new UserClient(builder.build());
    }

    @Test
    void getMe_existingUser_returnsUserResponse() {
        server.expect(requestTo("http://localhost/api/v1/telegram/users/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":"00000000-0000-0000-0000-000000000001",
                         "telegramId":42,
                         "journalUsername":"johndoe",
                         "createdAt":"2026-01-01T00:00:00Z",
                         "updatedAt":"2026-01-01T00:00:00Z"}
                        """, MediaType.APPLICATION_JSON));

        Optional<UserResponse> result = client.getMe(42L);

        assertThat(result).isPresent();
        assertThat(result.get().journalUsername()).isEqualTo("johndoe");
        assertThat(result.get().telegramId()).isEqualTo(42L);
        server.verify();
    }

    @Test
    void getMe_notFound_returnsEmpty() {
        server.expect(requestTo("http://localhost/api/v1/telegram/users/me"))
                .andRespond(withStatus(NOT_FOUND));

        Optional<UserResponse> result = client.getMe(99L);

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void register_returnsCreatedUser() {
        server.expect(requestTo("http://localhost/api/v1/telegram/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":"00000000-0000-0000-0000-000000000002",
                         "telegramId":55,
                         "journalUsername":"newuser",
                         "createdAt":"2026-01-01T00:00:00Z",
                         "updatedAt":"2026-01-01T00:00:00Z"}
                        """, MediaType.APPLICATION_JSON));

        UserResponse result = client.register(55L, "newuser", "pass123");

        assertThat(result.journalUsername()).isEqualTo("newuser");
        assertThat(result.telegramId()).isEqualTo(55L);
        server.verify();
    }
}
