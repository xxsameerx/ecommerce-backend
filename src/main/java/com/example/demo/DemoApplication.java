package com.example.demo;

import com.example.demo.controller.GameSeedController;
import com.example.demo.repository.GameRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner seedOnStartup(GameRepository gameRepository, GameSeedController seedController) {
        return args -> {
            if (gameRepository.count() == 0) {
                System.out.println("No games found, seeding from FreeToGame API...");
                seedController.seedGames();
            } else {
                System.out.println("Games already exist, skipping auto-seed.");
            }
        };
    }
}