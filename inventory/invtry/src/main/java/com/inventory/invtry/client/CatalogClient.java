package com.inventory.invtry.client;

import com.inventory.invtry.dto.CatalogBookResponseDto;
import com.inventory.invtry.exception.ResourceNotFoundException;
import com.inventory.invtry.exception.ServiceUnavailableException;
import com.inventory.invtry.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Inventory checks the Catalog service to confirm a bookId is a genuine,
 * catalogued book before stock is created or updated for it.
 */
@Component
public class CatalogClient {

    private final WebClient webClient;

    public CatalogClient(WebClient.Builder webClientBuilder,
                          @Value("${services.catalog.url:http://catalog}") String catalogServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(catalogServiceUrl).build();
    }

    public CatalogBookResponseDto getBookById(Long bookId) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId == null) {
            correlationId = "unknown";
        }
        try {
            return webClient.get()
                    .uri("/api/catalog.ctlog/{id}", bookId)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .retrieve()
                    .bodyToMono(CatalogBookResponseDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Book", "id", bookId);
        } catch (WebClientRequestException ex) {
            throw new ServiceUnavailableException("Catalog", ex.getMessage());
        }
    }
}
