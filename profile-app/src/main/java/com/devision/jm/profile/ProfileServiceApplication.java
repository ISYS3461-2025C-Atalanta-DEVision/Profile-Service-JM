package com.devision.jm.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Profile Service Application
 *
 * Main entry point for the Profile microservice.
 *
 * Microservice Architecture (A.3.1):
 * - Profile Service owns: user profiles, company info, subscription
 * - Communicates with Auth Service via Kafka (A.3.2)
 *
 * Features enabled:
 * - MongoDB Auditing: For createdAt/updatedAt timestamps
 * - Scheduling: For subscription notification jobs (6.1.2)
 *
 * Default port: 8082 (to avoid conflict with Auth Service on 8081)
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class ProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
