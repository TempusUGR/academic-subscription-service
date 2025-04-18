package com.calendarugr.academic_subscription_service.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String MAIL_EXCHANGE = "mail_exchange";
    public static final String MAIL_ROUTING_KEY = "notification_routing_key";

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(MAIL_EXCHANGE);
    }
}
