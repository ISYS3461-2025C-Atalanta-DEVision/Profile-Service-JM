package com.devision.jm.profile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
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
 * - Kafka: Conditionally enabled via KafkaConfig when kafka.enabled=true
 *
 * Note: KafkaAutoConfiguration is excluded to prevent startup failures
 * when Kafka is not configured. Custom KafkaConfig handles Kafka setup
 * conditionally based on kafka.enabled property.
 *
 * Default port: 8082 (to avoid conflict with Auth Service on 8081)
 */
@Slf4j
@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
@EnableMongoAuditing
@EnableScheduling
public class ProfileServiceApplication {

    @Value("${kafka.enabled:NOT_SET}")
    private String kafkaEnabled;

    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("========== PROFILE SERVICE STARTED ==========");
        log.info("kafka.enabled = {}", kafkaEnabled);
        log.info("==============================================");
    }
}
