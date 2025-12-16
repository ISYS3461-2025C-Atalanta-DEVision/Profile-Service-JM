package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.UserCreatedEvent;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.model.enums.SubscriptionType;
import com.devision.jm.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kafka Consumer for User Created Events
 *
 * Listens to the "user-created" topic from Auth Service.
 * Creates a new Profile document when a user registers.
 * Only loaded when kafka.enabled=true (KAFKA_ENABLED env var).
 *
 * Microservice Architecture (A.3.2):
 * - Consumes events from Auth Service via Kafka
 *
 * Flow:
 * 1. User registers in Auth Service
 * 2. Auth Service publishes UserCreatedEvent to Kafka
 * 3. This consumer receives the event
 * 4. Creates Profile document in Profile Service database
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class UserCreatedEventConsumer {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consume UserCreatedEvent from Kafka
     *
     * Topic: user-created
     * Group: profile-service-group
     */
    @KafkaListener(
            topics = "user-created",
            groupId = "${spring.kafka.consumer.group-id:profile-service-group}",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    @Transactional
    public void consumeUserCreatedEvent(String message) {
        log.info("Received user-created event: {}", message);

        try {
            // Deserialize JSON message to DTO
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);

            log.info("Processing UserCreatedEvent for userId: {}, email: {}",
                    event.getUserId(), event.getEmail());

            // Check if profile already exists (idempotency)
            if (profileRepository.existsByUserId(event.getUserId())) {
                log.warn("Profile already exists for userId: {}. Skipping creation.", event.getUserId());
                return;
            }

            // Create Profile from event data
            Profile profile = Profile.builder()
                    .userId(event.getUserId())
                    .email(event.getEmail())
                    .companyName(event.getCompanyName())
                    .country(event.getCountry())
                    .city(event.getCity())
                    .streetAddress(event.getStreetAddress())
                    .phoneNumber(event.getPhoneNumber())
                    .authProvider(event.getAuthProvider())
                    // New users start with FREE subscription (6.1.1)
                    .subscriptionType(SubscriptionType.FREE)
                    // avatarUrl will use default value from Profile entity
                    .build();

            // Save to database
            Profile savedProfile = profileRepository.save(profile);

            log.info("Profile created successfully for userId: {}, profileId: {}",
                    event.getUserId(), savedProfile.getId());

        } catch (Exception e) {
            log.error("Failed to process UserCreatedEvent: {}", e.getMessage(), e);
            // In production, consider dead letter queue for failed messages
            throw new RuntimeException("Failed to process UserCreatedEvent", e);
        }
    }
}
