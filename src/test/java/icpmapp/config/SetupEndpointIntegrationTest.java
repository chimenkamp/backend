package icpmapp.config;

import icpmapp.dto.requests.BootstrapSetupRequest;
import icpmapp.dto.requests.JwtAuthenticationResponse;
import icpmapp.dto.responses.PublicSiteConfigResponse;
import icpmapp.dto.responses.SetupStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SetupEndpointIntegrationTest {

    private static final Path DATABASE_BASE = Path.of(
        System.getProperty("java.io.tmpdir"),
        "conferia-setup-endpoint-" + System.nanoTime()
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> "jdbc:h2:file:" + DATABASE_BASE
        );
        registry.add("conferia.setup-token", () -> "endpoint-setup-token");
        registry.add(
            "conferia.jwt-secret",
            () -> "8HMAnIuSaJewPZpV2ah13uLOmySq7wE+kpmRWffu0rg="
        );
        registry.add("debug", () -> "false");
        registry.add("logging.level.root", () -> "WARN");
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    void bootstrapsEmptyDatabaseThenServesConfiguredConference() {
        SetupStatusResponse emptyStatus =
            rest.getForObject("/api/v1/setup/status", SetupStatusResponse.class);
        assertTrue(emptyStatus.isSetupRequired());
        assertFalse(emptyStatus.isSetupBlocked());

        BootstrapSetupRequest invalid = request("wrong-token");
        assertEquals(
            HttpStatus.FORBIDDEN,
            rest.postForEntity("/api/v1/setup/bootstrap", invalid, Map.class).getStatusCode()
        );

        ResponseEntity<JwtAuthenticationResponse> bootstrap = rest.postForEntity(
            "/api/v1/setup/bootstrap",
            request("endpoint-setup-token"),
            JwtAuthenticationResponse.class
        );
        assertEquals(HttpStatus.CREATED, bootstrap.getStatusCode());
        assertNotNull(bootstrap.getBody());
        assertNotNull(bootstrap.getBody().getAccessToken());
        assertNotNull(bootstrap.getBody().getRefreshToken());

        assertEquals(
            HttpStatus.CONFLICT,
            rest.postForEntity(
                "/api/v1/setup/bootstrap",
                request("endpoint-setup-token"),
                Map.class
            ).getStatusCode()
        );

        SetupStatusResponse partialStatus =
            rest.getForObject("/api/v1/setup/status", SetupStatusResponse.class);
        assertFalse(partialStatus.isSetupRequired());
        assertFalse(partialStatus.isConfigured());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bootstrap.getBody().getAccessToken());
        Map<String, Object> configuration = Map.of(
            "conference",
            Map.of(
                "name", "HTTP Conference",
                "location", "HTTP Venue",
                "timezone", "Europe/Berlin",
                "dates", Map.of("start", "2030-06-10", "end", "2030-06-12")
            )
        );
        ResponseEntity<Void> update = rest.exchange(
            "/api/v1/admin/config/bulk",
            HttpMethod.POST,
            new HttpEntity<>(configuration, headers),
            Void.class
        );
        assertEquals(HttpStatus.OK, update.getStatusCode());

        SetupStatusResponse readyStatus =
            rest.getForObject("/api/v1/setup/status", SetupStatusResponse.class);
        assertTrue(readyStatus.isConfigured());
        PublicSiteConfigResponse publicConfig =
            rest.getForObject("/api/v1/config/site", PublicSiteConfigResponse.class);
        assertEquals("HTTP Conference", publicConfig.getConference().getName());
    }

    private BootstrapSetupRequest request(String token) {
        BootstrapSetupRequest request = new BootstrapSetupRequest();
        request.setSetupToken(token);
        request.setEmail("first-admin@example.test");
        request.setPassword("twelve-characters");
        request.setFirstname("First");
        request.setLastname("Administrator");
        return request;
    }
}
