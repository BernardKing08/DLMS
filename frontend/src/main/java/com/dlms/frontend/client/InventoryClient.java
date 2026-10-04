package com.dlms.frontend.client;

import com.dlms.frontend.dto.BackendErrorDto;
import com.dlms.frontend.dto.BorrowRecordDto;
import com.dlms.frontend.dto.BorrowRequestDto;
import com.dlms.frontend.dto.InventoryResponseDto;
import com.dlms.frontend.dto.InventoryUpdateRequestDto;
import com.dlms.frontend.exception.ServiceUnavailableException;
import com.dlms.frontend.exception.ValidationException;
import com.dlms.frontend.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/** Talks to the Inventory service - manages stock counts and borrowing records. */
@Component
public class InventoryClient {

    private final WebClient webClient;

    public InventoryClient(WebClient.Builder webClientBuilder,
                            @Value("${services.inventory.url:http://inventory}") String inventoryServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(inventoryServiceUrl).build();
    }

    public List<InventoryResponseDto> getAll() {
        try {
            return webClient.get()
                    .uri("/api/inventory")
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, currentCorrelationId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InventoryResponseDto>>() {})
                    .block();
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Inventory", ex.getMessage());
        }
    }

    public InventoryResponseDto update(Long bookId, int totalCopies) {
        InventoryUpdateRequestDto request = new InventoryUpdateRequestDto();
        request.setBookId(bookId);
        request.setTotalCopies(totalCopies);
        try {
            return webClient.post()
                    .uri("/api/inventory")
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, currentCorrelationId())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(InventoryResponseDto.class)
                    .block();
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Inventory", ex.getMessage());
        }
    }

    public List<BorrowRecordDto> borrowBooks(Long userId, List<Long> bookIds, String pickupLocation, String notes) {
        BorrowRequestDto request = BorrowRequestDto.builder()
                .userId(userId)
                .bookIds(bookIds)
                .pickupLocation(pickupLocation)
                .notes(notes)
                .build();
        try {
            return webClient.post()
                    .uri("/api/inventory/borrow")
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, currentCorrelationId())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BorrowRecordDto>>() {})
                    .block();
        } catch (WebClientResponseException.BadRequest ex) {
            throw new ValidationException(extractMessage(ex, "Failed to complete borrowing request."));
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Inventory", ex.getMessage());
        }
    }

    public List<BorrowRecordDto> getLoansByUserId(Long userId) {
        try {
            return webClient.get()
                    .uri("/api/inventory/loans/user/{userId}", userId)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, currentCorrelationId())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BorrowRecordDto>>() {})
                    .block();
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Inventory", ex.getMessage());
        }
    }

    public BorrowRecordDto returnBook(Long loanId) {
        try {
            return webClient.post()
                    .uri("/api/inventory/loans/{id}/return", loanId)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, currentCorrelationId())
                    .retrieve()
                    .bodyToMono(BorrowRecordDto.class)
                    .block();
        } catch (WebClientResponseException.BadRequest ex) {
            throw new ValidationException(extractMessage(ex, "Failed to return book."));
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            throw new ServiceUnavailableException("Inventory", ex.getMessage());
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

