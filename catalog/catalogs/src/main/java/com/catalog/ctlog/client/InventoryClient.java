package com.catalog.ctlog.client;

import com.catalog.ctlog.dto.InventoryRequestDto;
import com.catalog.ctlog.dto.InventoryResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        webClient.post()
                .uri("/api/inventory")
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
        try {
            return Optional.ofNullable(
                    webClient.get()
                            .uri("/api/inventory/{bookId}", bookId)
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
