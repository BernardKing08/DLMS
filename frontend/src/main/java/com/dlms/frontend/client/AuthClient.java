package com.dlms.frontend.client;

import com.dlms.frontend.dto.BackendErrorDto;
import com.dlms.frontend.dto.LoginRequestDto;
import com.dlms.frontend.dto.RegisterRequestDto;
import com.dlms.frontend.dto.UserResponseDto;
import com.dlms.frontend.exception.ServiceUnavailableException;
import com.dlms.frontend.exception.ValidationException;
import com.dlms.frontend.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Talks to the Authentication service for login and registration - same
 * WebClient + Eureka-service-name pattern every other cross-service client
 * in this project uses.
 */
@Component
public class AuthClient {

    private final WebClient webClient;

    public AuthClient(WebClient.Builder webClientBuilder,
                       @Value("${services.auth.url:http://authentication}") String authServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    public UserResponseDto login(LoginRequestDto request) {
        String correlationId = currentCorrelationId();
        try {
            return webClient.post()
                    .uri("/api/auth/login")
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(UserResponseDto.class)
                    .block();
        } catch (WebClientResponseException.BadRequest ex) {
            throw new ValidationException(extractMessage(ex, "Invalid email or password"));
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Authentication", ex.getMessage());
        }
    }

    public void register(RegisterRequestDto request) {
        String correlationId = currentCorrelationId();
        try {
            webClient.post()
                    .uri("/api/auth/register")
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException.BadRequest ex) {
            throw new ValidationException(extractMessage(ex, "Registration failed"));
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Authentication", ex.getMessage());
        }
    }

    private String extractMessage(WebClientResponseException.BadRequest ex, String fallback) {
        try {
            BackendErrorDto error = ex.getResponseBodyAs(BackendErrorDto.class);
            return (error != null && error.getErrorMsg() != null) ? error.getErrorMsg() : fallback;
        } catch (Exception parseFailure) {
            return fallback;
        }
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId != null ? correlationId : "unknown";
    }
}
