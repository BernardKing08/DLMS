package com.dlms.auth.client;

import com.dlms.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Notifies the Account service so a profile can be provisioned whenever a
 * new user registers. Best-effort: a failure here must never block
 * registration, since the Account service is downstream of Auth.
 */
@Component
public class AccountClient {

    private static final Logger log = LoggerFactory.getLogger(AccountClient.class);

    private final WebClient webClient;

    public AccountClient(WebClient.Builder webClientBuilder,
                          @Value("${services.account.url:http://account}") String accountServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(accountServiceUrl).build();
    }

    public void createAccountFor(User user) {
        String[] nameParts = user.getName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        webClient.post()
                .uri("/api/accounts")
                .bodyValue(Map.of(
                        "userId", user.getId(),
                        "firstName", firstName,
                        "lastName", lastName
                ))
                .retrieve()
                .toBodilessEntity()
                .doOnError(ex -> log.warn(
                        "Failed to provision account profile for userId={}: {}",
                        user.getId(), ex.getMessage()))
                .onErrorComplete()
                .subscribe();
    }
}
