package com.se170395.orchid;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"controller", "service", "repository", "pojo", "config","security"})
@EntityScan(basePackages = {"pojo"})
@EnableJpaRepositories(basePackages = "repository")
public class OrchidApplication   {

	public static void main(String[] args) {
		SpringApplication.run(OrchidApplication.class, args);
	}

}
