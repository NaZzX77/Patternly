package com.patternly.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PatternlyBackendApplicationTests {
	@LocalServerPort
	private int port;

	@Test
	void healthEndpointReportsUp() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(
				"http://localhost:" + port + "/actuator/health")).GET().build();
		HttpResponse<String> response = HttpClient.newHttpClient().send(
				request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"status\":\"UP\"");
	}

}
