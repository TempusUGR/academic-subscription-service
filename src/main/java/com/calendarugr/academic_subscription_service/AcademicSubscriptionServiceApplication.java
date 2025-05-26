package com.calendarugr.academic_subscription_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableDiscoveryClient
public class AcademicSubscriptionServiceApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load();
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("API_KEY", dotenv.get("API_KEY"));
		System.setProperty("FTP_PASSWORD", dotenv.get("FTP_PASSWORD"));
		System.setProperty("FTP_USERNAME", dotenv.get("FTP_USERNAME"));
		System.setProperty("FTP_HOST", dotenv.get("FTP_HOST"));
		System.setProperty("FTP_PORT", dotenv.get("FTP_PORT"));
		System.setProperty("EUREKA_URL", dotenv.get("EUREKA_URL"));
		System.setProperty("RABBITMQ_HOST", dotenv.get("RABBITMQ_HOST"));
		System.setProperty("RABBITMQ_PORT", dotenv.get("RABBITMQ_PORT"));
		System.setProperty("RABBITMQ_USERNAME", dotenv.get("RABBITMQ_USERNAME"));
		System.setProperty("RABBITMQ_PASSWORD", dotenv.get("RABBITMQ_PASSWORD"));
		System.setProperty("SECRET_KEY", dotenv.get("SECRET_KEY"));
		SpringApplication.run(AcademicSubscriptionServiceApplication.class, args);
	}

}
