package com.dlms.frontend.client;

import com.dlms.frontend.dto.BookResponseDto;
import com.dlms.frontend.exception.ResourceNotFoundException;
import com.dlms.frontend.exception.ServiceUnavailableException;
import com.dlms.frontend.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * Talks to the Catalog service for book listings and detail. Catalog's own
 * response is already enriched with live stock numbers from Inventory, so
 * this is the only client the frontend needs for the book pages.
 */
@Component
public class CatalogClient {

    private final WebClient webClient;

    public CatalogClient(WebClient.Builder webClientBuilder,
                          @Value("${services.catalog.url:http://catalog}") String catalogServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(catalogServiceUrl).build();
    }

    public List<BookResponseDto> getAllBooks() {
        String correlationId = currentCorrelationId();
        try {
            return webClient.get()
                    .uri("/api/catalog")
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<BookResponseDto>>() {})
                    .block();
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            // ServiceUnavailable (503) is what Spring Cloud LoadBalancer throws when
            // Eureka has no registered/healthy instance for this service - a
            // different failure mode than a connection-level WebClientRequestException.
            throw new ServiceUnavailableException("Catalog", ex.getMessage());
        }
    }

    public BookResponseDto getById(Long id) {
        String correlationId = currentCorrelationId();
        try {
            return webClient.get()
                    .uri("/api/catalog/{id}", id)
                    .header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId)
                    .retrieve()
                    .bodyToMono(BookResponseDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Book", "id", id);
        } catch (WebClientResponseException.ServiceUnavailable | WebClientRequestException ex) {
            // ServiceUnavailable (503) is what Spring Cloud LoadBalancer throws when
            // Eureka has no registered/healthy instance for this service - a
            // different failure mode than a connection-level WebClientRequestException.
            throw new ServiceUnavailableException("Catalog", ex.getMessage());
        }
    }

    private String currentCorrelationId() {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        return correlationId != null ? correlationId : "unknown";
    }
}
