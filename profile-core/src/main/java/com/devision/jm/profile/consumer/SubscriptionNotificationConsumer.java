package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.SubscriptionChangedEvent;
import com.devision.jm.profile.api.internal.dto.SubscriptionNotificationEvent;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.model.enums.SubscriptionType;
import com.devision.jm.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Kafka Consumer for Subscription Notification Events
 *
 * Listens to the "subscription-notifications" topic from Payment Service.
 * Updates profile subscription status when subscription expires.
 * Only loaded when kafka.enabled=true (KAFKA_ENABLED env var).
 *
 * Flow:
 * 1. Payment Service scheduled job detects expiring/expired subscriptions
 * 2. Payment Service publishes SubscriptionNotificationEvent to Kafka
 * 3. This consumer receives the event
 * 4. For ENDING_SOON: Sets expiryNotificationSent = true
 * 5. For ENDED: Sets expiredNotificationSent = true + subscriptionType = FREE
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@Transactional(rollbackFor = Exception.class)
public class SubscriptionNotificationConsumer {

    private static final String SUBSCRIPTION_CHANGED_TOPIC = "subscription.changed";

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Consume SubscriptionNotificationEvent from Kafka
     *
     * Topic: subscription-notifications
     * Group: profile-service-group
     */
    @KafkaListener(
            topics = "subscription-notifications",
            groupId = "${spring.kafka.consumer.group-id:profile-service-group}"
    )
    public void consumeSubscriptionNotificationEvent(String message) {
        log.info("========== RECEIVED SUBSCRIPTION NOTIFICATION EVENT ==========");
        log.info("Message: {}", message);

        try {
            // Deserialize JSON message to DTO
            SubscriptionNotificationEvent event = objectMapper.readValue(message, SubscriptionNotificationEvent.class);

            log.info("Processing SubscriptionNotificationEvent: eventType={}, userId={}, daysLeft={}",
                    event.getEventType(), event.getUserId(), event.getDaysLeft());

            // Find profile by userId
            Profile profile = profileRepository.findByUserId(event.getUserId()).orElse(null);

            if (profile == null) {
                log.error("Profile not found for userId: {}. Cannot update subscription status.", event.getUserId());
                return;
            }

            // Handle based on event type
            if ("ENDING_SOON".equals(event.getEventType())) {
                // Mark that expiry notification was sent
                profile.setExpiryNotificationSent(true);
                profileRepository.save(profile);

                log.info("✅ Updated expiryNotificationSent=true for userId={}", event.getUserId());

            } else if ("ENDED".equals(event.getEventType())) {
                // Mark that expired notification was sent + downgrade to FREE
                profile.setExpiredNotificationSent(true);
                profile.setSubscriptionType(SubscriptionType.FREE);
                profileRepository.save(profile);

                log.info("✅ Updated subscriptionType=FREE, expiredNotificationSent=true for userId={}", event.getUserId());

                // Publish subscription changed event for Applicant-Search-Service
                publishSubscriptionChangedEvent(event.getUserId(), false);
            } else {
                log.warn("Unknown eventType: {}", event.getEventType());
            }

            log.info("========== SUBSCRIPTION NOTIFICATION PROCESSED ==========");

        } catch (Exception e) {
            log.error("Failed to process SubscriptionNotificationEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process SubscriptionNotificationEvent", e);
        }
    }

    /**
     * Publish subscription changed event to Kafka
     * Consumed by Applicant-Search-Service to update isPremium flag
     */
    private void publishSubscriptionChangedEvent(String companyId, boolean isPremium) {
        try {
            SubscriptionChangedEvent event = SubscriptionChangedEvent.builder()
                    .companyId(companyId)
                    .isPremium(isPremium)
                    .subscriptionType(isPremium ? "PREMIUM" : "FREE")
                    .changedAt(LocalDateTime.now())
                    .build();

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(SUBSCRIPTION_CHANGED_TOPIC, companyId, json);

            log.info("Published subscription changed event: companyId={}, isPremium={}", companyId, isPremium);
        } catch (Exception e) {
            log.error("Failed to publish subscription changed event: {}", e.getMessage(), e);
        }
    }
}
