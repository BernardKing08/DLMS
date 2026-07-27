package com.account.acc.client;

import com.account.acc.dto.AuthUserResponseDto;
import com.account.acc.exception.ResourceNotFoundException;
import com.account.acc.exception.ServiceUnavailableException;
import com.account.acc.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Talks to the Authentication service to confirm a userId is a genuine,
 * registered user before an Account profile is created for it.
 */
@Component
public class AuthClient {

    private final WebClient webClient;

    public AuthClient(WebClient.Builder webClientBuilder,
                       @Value("${services.auth.url:http://authentication}") String authServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    public AuthUserResponseDto getUserById(Long userId) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId == null) {
            correlationId = "unknown";
        }
        try {
            return webClient.get()
                    .uri("/api/auth/users/{id}", userId)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .retrieve()
                    .bodyToMono(AuthUserResponseDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("User", "userId", userId);
        } catch (WebClientRequestException ex) {
            throw new ServiceUnavailableException("Authentication", ex.getMessage());
        }
    }
}
