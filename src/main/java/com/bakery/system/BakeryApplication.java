package com.bakery.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.bakery.system.repository")
public class BakeryApplication {
    public static void main(String[] args) {
        SpringApplication.run(BakeryApplication.class, args);
        System.out.println("========================================");
        System.out.println("  🍞 Benthota Bake House Started! 🍰");
        System.out.println("  Server running at: http://localhost:8080");
        System.out.println("========================================");
    }
}