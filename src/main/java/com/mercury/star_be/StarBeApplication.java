package com.mercury.star_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StarBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StarBeApplication.class, args);
	}

}
