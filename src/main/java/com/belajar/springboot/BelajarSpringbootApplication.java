package com.belajar.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BelajarSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BelajarSpringbootApplication.class, args);
	}

}
