package br.com.fiap.predit.risk.api;

import br.com.fiap.predit.risk.support.TestTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe o servidor real (Tomcat + filtros) para garantir que erros nao sao mascarados
 * como 403 pelo encaminhamento interno ao /error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpErrorResponseTest {
    @Autowired TestRestTemplate rest;

    private HttpHeaders adminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, TestTokens.bearer("ADMIN"));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void malformedJsonIsBadRequestOverRealHttp() {
        ResponseEntity<String> response = rest.exchange("/api/v1/campaigns", HttpMethod.POST,
                new HttpEntity<>("{bad", adminHeaders()), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void unknownEndpointIsNotFoundOverRealHttp() {
        ResponseEntity<String> response = rest.exchange("/api/v1/does-not-exist", HttpMethod.GET,
                new HttpEntity<>(adminHeaders()), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void anonymousRequestIsUnauthorizedOverRealHttp() {
        ResponseEntity<String> response = rest.getForEntity("/api/v1/dashboard/summary", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("Authentication required");
    }
}
