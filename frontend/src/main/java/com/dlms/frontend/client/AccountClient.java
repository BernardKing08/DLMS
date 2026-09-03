package com.dlms.frontend.client;

import com.dlms.frontend.dto.AccountResponseDto;
import com.dlms.frontend.dto.AccountUpdateRequestDto;
import com.dlms.frontend.dto.BackendErrorDto;
import com.dlms.frontend.exception.ResourceNotFoundException;
import com.dlms.frontend.exception.ServiceUnavailableException;
import com.dlms.frontend.exception.ValidationException;
import com.dlms.frontend.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** Talks to the Account service to read and update a user's profile. */
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient.Builder webClientBuilder,
                          @Value("${services.account.url:http://account}") String accountServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
    }

    public AccountResponseDto getByUserId(Long userId) {
        String correlationId = currentCorrelationId();
        try {
            return webClient.get()
                    .uri("/api/accounts/{userId}", userId)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .retrieve()
                    .bodyToMono(AccountResponseDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Account", "userId", userId);
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Account", ex.getMessage());
        }
    }

    public AccountResponseDto update(Long userId, AccountUpdateRequestDto request) {
        String correlationId = currentCorrelationId();
        try {
            return webClient.put()
                    .uri("/api/accounts/{userId}", userId)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AccountResponseDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Account", "userId", userId);
        } catch (WebClientResponseException.BadRequest ex) {
            throw new ValidationException(extractMessage(ex, "Could not update profile"));
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Account", ex.getMessage());
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
