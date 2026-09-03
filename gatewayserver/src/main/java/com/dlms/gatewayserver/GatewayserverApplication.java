package com.dlms.gatewayserver;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserverApplication.class, args);
	}

	@Bean
	public RouteLocator bennylibrary(RouteLocatorBuilder routeLocatorBuilder){
		return routeLocatorBuilder.routes()
			.route(p -> p.path("/dlms/account/**")
				.filters(f -> f.rewritePath("/dlms/account/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
				.uri("lb://ACCOUNT"))
			.route(p -> p.path("/dlms/authentication/**")
				.filters(f -> f.rewritePath("/dlms/authentication/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
				.uri("lb://AUTHENTICATION"))
			.route(p -> p.path("/dlms/catalog/**")
				.filters(f -> f.rewritePath("/dlms/catalog/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
				.uri("lb://CATALOG"))
			.route(p -> p.path("/dlms/inventory/**")
				.filters(f -> f.rewritePath("/dlms/inventory/(?<segment>.*)", "/${segment}")
				.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
				.uri("lb://INVENTORY"))
			// Catch-all, must stay LAST - routes match top-to-bottom, first
			// match wins. Anything not matched by the /dlms/* API routes
			// above (pages, static assets) falls through to the frontend.
			.route(p -> p.path("/**")
				.uri("lb://FRONTEND"))
			.build();
	}

}
