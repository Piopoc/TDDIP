package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This class orchestrates the bootstrapping of the Spring Boot framework and
 * serves as the central configuration hub
 *
 * It integrates multiple data persistence layers and enables asynchronous
 * task execution to handle computationally intensive Text Analytics processes without blocking
 * the main execution thread
 */
@SpringBootApplication
@EnableAsync
@EnableMongoRepositories(basePackages = "com.example.demo.repository.mongo")
@EnableElasticsearchRepositories(basePackages = "com.exaple.demo.repository.elastic")
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}