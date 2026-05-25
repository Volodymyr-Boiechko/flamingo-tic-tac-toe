package com.flamingo.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GameSessionApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameSessionApplication.class, args);
    }
}
