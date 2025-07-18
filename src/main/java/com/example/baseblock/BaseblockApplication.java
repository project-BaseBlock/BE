package com.example.baseblock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BaseblockApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaseblockApplication.class, args);
	}

}
