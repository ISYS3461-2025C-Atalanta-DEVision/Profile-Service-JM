package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.PaymentCompletedEvent;
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
 * Kafka Consumer for Payment Completed Events
 *
 * Listens to the "payment-completed" topic from Payment Service.
 * Upgrades user subscription from FREE to PREMIUM when payment succeeds.
 * Only loaded when kafka.enabled=true (KAFKA_ENABLED env var).
 *
 * Microservice Architecture (A.3.2):
 * - Consumes events from Payment Service via Kafka
 *
 * Flow:
 * 1. User completes payment in Payment Service
 * 2. Payment Service publishes PaymentCompletedEvent to Kafka
 * 3. This consumer receives the event
 * 4. Updates Profile subscription to PREMIUM
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class PaymentCompletedEventConsumer {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consume PaymentCompletedEvent from Kafka
     *
     * Topic: payment-completed
     * Group: profile-service-group
     */
    @KafkaListener(
            topics = "payment-completed",
            groupId = "${spring.kafka.consumer.group-id:profile-service-group}",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    @Transactional
    public void consumePaymentCompletedEvent(String message) {
        log.info("========== RECEIVED PAYMENT COMPLETED EVENT ==========");
        log.info("Message: {}", message);

        try {
            // Deserialize JSON message to DTO
            PaymentCompletedEvent event = objectMapper.readValue(message, PaymentCompletedEvent.class);

            log.info("Processing PaymentCompletedEvent for userId: {}, planType: {}",
                    event.getUserId(), event.getPlanType());

            // Find profile by userId
            Profile profile = profileRepository.findByUserId(event.getUserId())
                    .orElse(null);

            if (profile == null) {
                log.error("Profile not found for userId: {}. Cannot upgrade subscription.", event.getUserId());
                return;
            }

            // Check if already PREMIUM (idempotency)
            if (profile.getSubscriptionType() == SubscriptionType.PREMIUM) {
                log.warn("Profile already has PREMIUM subscription for userId: {}. Extending subscription.",
                        event.getUserId());
            }

            // Update subscription to PREMIUM
            profile.setSubscriptionType(SubscriptionType.PREMIUM);
            profile.setSubscriptionStartDate(event.getPaidAt() != null ? event.getPaidAt() : LocalDateTime.now());
            profile.setSubscriptionEndDate(profile.getSubscriptionStartDate().plusDays(30));
            profile.setExpiryNotificationSent(false);
            profile.setExpiredNotificationSent(false);

            // Save to database
            Profile savedProfile = profileRepository.save(profile);

            log.info("========== SUBSCRIPTION UPGRADED TO PREMIUM ==========");
            log.info("UserId: {}", event.getUserId());
            log.info("ProfileId: {}", savedProfile.getId());
            log.info("SubscriptionType: {}", savedProfile.getSubscriptionType());
            log.info("SubscriptionStartDate: {}", savedProfile.getSubscriptionStartDate());
            log.info("SubscriptionEndDate: {}", savedProfile.getSubscriptionEndDate());
            log.info("======================================================");

        } catch (Exception e) {
            log.error("Failed to process PaymentCompletedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PaymentCompletedEvent", e);
        }
    }
}
