package com.catalog.ctlog.client;

import com.catalog.ctlog.dto.InventoryRequestDto;
import com.catalog.ctlog.dto.InventoryResponseDto;
import com.catalog.ctlog.filter.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Optional;

/**
 * Talks to the Inventory service so a newly catalogued book gets a stock
 * record, and so book responses can be enriched with live stock levels.
 * Both directions are best-effort: Catalog owns book data and must keep
 * working even if Inventory is unreachable.
 */
@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final WebClient webClient;

    public InventoryClient(WebClient.Builder webClientBuilder,
                            @Value("${services.inventory.url:http://inventory}") String inventoryServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(inventoryServiceUrl).build();
    }

    public void createInventory(Long bookId, int totalCopies) {
        // Read MDC now, on the calling (request) thread - see AccountClient
        // in the auth service for why this can't be deferred into a lambda.
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId == null) {
            correlationId = "unknown";
        }

        webClient.post()
                .uri("/api/inventory")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                .bodyValue(new InventoryRequestDto(bookId, totalCopies))
                .retrieve()
                .toBodilessEntity()
                .doOnError(ex -> log.warn(
                        "Failed to create inventory record for bookId={}: {}",
                        bookId, ex.getMessage()))
                .onErrorComplete()
                .subscribe();
    }

    public Optional<InventoryResponseDto> getInventoryByBookId(Long bookId) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId == null) {
            correlationId = "unknown";
        }
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri("/api/inventory/{bookId}", bookId)
                            .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                            .retrieve()
                            .bodyToMono(InventoryResponseDto.class)
                            .block()
            );
        } catch (WebClientException ex) {
            log.warn("Failed to fetch inventory for bookId={}: {}", bookId, ex.getMessage());
            return Optional.empty();
        }
    }
}
