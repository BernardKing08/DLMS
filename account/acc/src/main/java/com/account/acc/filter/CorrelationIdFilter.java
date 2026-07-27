package com.account.acc.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Reads (or generates) a correlation ID for every incoming request, makes it
 * available to every log line via MDC, and echoes it back on the response -
 * same mechanism as the gateway's RequestTraceFilter/ResponseTraceFilter,
 * just at the individual-service level so this still works even when a
 * request arrives directly, bypassing the gateway (e.g. local testing).
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "eazybank-correlation-id";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Tomcat reuses threads across requests - without this, a later
            // unrelated request handled by the same pooled thread could
            // inherit this correlation ID.
            MDC.remove(MDC_KEY);
        }
    }
}
