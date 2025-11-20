package com.jelly.jellyai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JellyAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JellyAiApplication.class, args);
	}

}
