package com.calendarugr.academic_subscription_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// UNCOMMENT TO USE ZIPKIN
// import io.micrometer.observation.ObservationRegistry;
// import org.springframework.web.reactive.function.client.DefaultClientRequestObservationConvention;

@Configuration
public class WebClientConfig {
    
    // UNCOMMENT TO USE ZIPKIN
    // @Bean
    // @LoadBalanced
    // public WebClient.Builder webClientBuilder(ObservationRegistry observationRegistry) {
    //     return WebClient.builder()
    //             .observationRegistry(observationRegistry)
    //             .observationConvention(new DefaultClientRequestObservationConvention());
    // }

    //COMMENT TO NOT TO USE ZIPKIN -----------------------
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    // ---------------------------------------------------
    
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
    
}
