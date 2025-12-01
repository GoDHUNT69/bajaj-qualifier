package com.example.bfhl;

import com.example.bfhl.service.QualifierService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BfhlQualifierApplication {

    public static void main(String[] args) {
        SpringApplication.run(BfhlQualifierApplication.class, args);
    }

    // Runs exactly once at startup (no HTTP calls needed)
    @Bean
    public CommandLineRunner runOnStartup(QualifierService qualifierService) {
        return args -> qualifierService.executeFlow();
    }
}
