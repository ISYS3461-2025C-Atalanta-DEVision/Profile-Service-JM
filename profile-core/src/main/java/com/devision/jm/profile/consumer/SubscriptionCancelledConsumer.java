package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.SubscriptionCancelledEvent;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.model.enums.SubscriptionType;
import com.devision.jm.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Subscription Cancelled Event Consumer
 *
 * Consumes subscription-cancelled events from Payment Service.
 * Downgrades the user's subscription to FREE when they cancel.
 *
 * Topic: subscription-cancelled
 * Producer: Payment Service
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionCancelledConsumer {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionCancelledConsumer(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "subscription-cancelled", groupId = "${spring.kafka.consumer.group-id:profile-service-group}")
    public void consumeSubscriptionCancelledEvent(String message) {
        log.info("========== RECEIVED SUBSCRIPTION CANCELLED EVENT ==========");
        log.info("Raw message: {}", message);

        try {
            SubscriptionCancelledEvent event = objectMapper.readValue(message, SubscriptionCancelledEvent.class);

            log.info("Parsed event - UserId: {}, CompanyId: {}, ApplicantId: {}, PreviousPlanType: {}",
                    event.getUserId(), event.getCompanyId(), event.getApplicantId(), event.getPreviousPlanType());

            // Find profile by userId (which is the companyId for company subscriptions)
            String userId = event.getUserId();
            if (userId == null) {
                userId = event.getCompanyId() != null ? event.getCompanyId() : event.getApplicantId();
            }

            if (userId == null) {
                log.error("No userId, companyId, or applicantId in event - cannot process");
                return;
            }

            Optional<Profile> profileOpt = profileRepository.findByUserId(userId);

            if (profileOpt.isEmpty()) {
                log.warn("Profile not found for userId: {}", userId);
                return;
            }

            Profile profile = profileOpt.get();

            // Downgrade to FREE
            profile.setSubscriptionType(SubscriptionType.FREE);
            profile.setSubscriptionEndDate(null);

            profileRepository.save(profile);

            log.info("========== SUBSCRIPTION DOWNGRADED TO FREE ==========");
            log.info("UserId: {}", userId);
            log.info("Previous Plan: {}", event.getPreviousPlanType());
            log.info("New Plan: FREE");
            log.info("=====================================================");

        } catch (Exception e) {
            log.error("Failed to process subscription cancelled event: {}", e.getMessage(), e);
        }
    }
}
