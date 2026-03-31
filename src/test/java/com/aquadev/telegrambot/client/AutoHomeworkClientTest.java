package com.aquadev.telegrambot.client;

import com.aquadev.telegrambot.client.dto.AutoHomeworkSettingsResponse;
import com.aquadev.telegrambot.client.dto.UpdateAutoHomeworkSettingsRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Set;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AutoHomeworkClientTest {

    private MockRestServiceServer server;
    private AutoHomeworkClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new AutoHomeworkClient(builder.build());
    }

    @Test
    void getSettings_returnsSettingsResponse() {
        server.expect(requestTo("http://localhost/api/v1/telegram/auto-homework/settings"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"enabled":true,"lastCheckedAt":null,"specIds":[1,2]}
                        """, MediaType.APPLICATION_JSON));

        AutoHomeworkSettingsResponse result = client.getSettings(10L);

        assertThat(result.enabled()).isTrue();
        assertThat(result.specIds()).containsExactlyInAnyOrder(1L, 2L);
        server.verify();
    }

    @Test
    void updateSettings_sendsPutAndReturnsUpdated() {
        server.expect(requestTo("http://localhost/api/v1/telegram/auto-homework/settings"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("""
                        {"enabled":false,"lastCheckedAt":null,"specIds":[]}
                        """, MediaType.APPLICATION_JSON));

        var request = new UpdateAutoHomeworkSettingsRequest(false, Set.of());
        AutoHomeworkSettingsResponse result = client.updateSettings(10L, request);

        assertThat(result.enabled()).isFalse();
        assertThat(result.specIds()).isEmpty();
        server.verify();
    }

    @Test
    void getGroupSpecs_returnsSpecList() {
        server.expect(requestTo("http://localhost/api/v1/telegram/journal/group-specs"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":1,"name":"Mathematics","shortName":"MATH"},
                         {"id":2,"name":"Physics","shortName":"PHYS"}]
                        """, MediaType.APPLICATION_JSON));

        var result = client.getGroupSpecs(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Mathematics");
        assertThat(result.get(1).shortName()).isEqualTo("PHYS");
        server.verify();
    }
}
