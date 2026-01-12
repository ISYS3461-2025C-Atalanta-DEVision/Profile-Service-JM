package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.UserDeletedEvent;
import com.devision.jm.profile.model.entity.Event;
import com.devision.jm.profile.repository.EventRepository;
import com.devision.jm.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kafka Consumer for User Deleted Events
 *
 * Listens to the "user-deleted" topic from Auth Service.
 * Cleans up profile and related data when a user is deleted.
 * Only loaded when kafka.enabled=true (KAFKA_ENABLED env var).
 *
 * Microservice Architecture (A.3.2):
 * - Consumes events from Auth Service via Kafka
 *
 * Flow:
 * 1. Admin deletes user in Auth Service
 * 2. Auth Service publishes UserDeletedEvent to Kafka
 * 3. This consumer receives the event
 * 4. Deletes Profile document and all company Events
 *
 * Cleanup Actions:
 * - Delete profile document from profiles collection
 * - Delete all events for the company from events collection
 * - (Future) Publish event to File Service to delete S3 files
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@Transactional(rollbackFor = Exception.class)
public class UserDeletedEventConsumer {

    private final ProfileRepository profileRepository;
    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consume UserDeletedEvent from Kafka
     *
     * Topic: user-deleted
     * Group: profile-service-group
     */
    @KafkaListener(
            topics = "user-deleted",
            groupId = "${spring.kafka.consumer.group-id:profile-service-group}",
            autoStartup = "${kafka.consumer.auto-startup:true}"
    )
    public void consumeUserDeletedEvent(String message) {
        log.info("Received user-deleted event: {}", message);

        try {
            // Deserialize JSON message to DTO
            UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);

            log.info("Processing UserDeletedEvent for userId: {}, email: {}",
                    event.getUserId(), event.getEmail());

            String userId = event.getUserId();

            // 1. Delete all events for this company
            deleteCompanyEvents(userId);

            // 2. Delete the profile
            deleteProfile(userId);

            log.info("Successfully cleaned up data for deleted user. userId={}, email={}",
                    event.getUserId(), event.getEmail());

        } catch (Exception e) {
            log.error("Failed to process UserDeletedEvent: {}", e.getMessage(), e);
            // In production, consider dead letter queue for failed messages
            throw new RuntimeException("Failed to process UserDeletedEvent", e);
        }
    }

    /**
     * Delete all events for a company
     */
    private void deleteCompanyEvents(String userId) {
        try {
            // First, get the events to log what we're deleting
            List<Event> events = eventRepository.findByCompanyId(userId);

            if (events.isEmpty()) {
                log.info("No events found for userId={}. Skipping event deletion.", userId);
                return;
            }

            log.info("Deleting {} events for userId={}", events.size(), userId);

            // TODO: Publish events to File Service to delete S3 files (images, videos)
            // for (Event event : events) {
            //     if (event.getMediaUrls() != null && !event.getMediaUrls().isEmpty()) {
            //         // Publish file deletion request to Kafka
            //     }
            // }

            // Delete all events
            eventRepository.deleteByCompanyId(userId);
            log.info("Deleted {} events for userId={}", events.size(), userId);

        } catch (Exception e) {
            log.error("Failed to delete events for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Delete the profile document
     */
    private void deleteProfile(String userId) {
        try {
            if (!profileRepository.existsByUserId(userId)) {
                log.warn("Profile not found for userId={}. May have been already deleted.", userId);
                return;
            }

            // TODO: Get avatar URL and publish to File Service for S3 deletion
            // Profile profile = profileRepository.findByUserId(userId).orElse(null);
            // if (profile != null && profile.getAvatarUrl() != null) {
            //     // Publish avatar deletion request to Kafka
            // }

            profileRepository.deleteByUserId(userId);
            log.info("Deleted profile for userId={}", userId);

        } catch (Exception e) {
            log.error("Failed to delete profile for userId={}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}
