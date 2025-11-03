package com.chelv.authentication_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = { "com.chelv.repository" })
@EntityScan(basePackages = { "com.chelv.entities" })
@ComponentScan(basePackages = 
{ "com.chelv.controller",
  "com.chelv.auth",
  "com.chelv.service",
  "com.chelv.pubsub.publish.eventPublisher" })
public class AuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationServiceApplication.class, args);
	}

}
