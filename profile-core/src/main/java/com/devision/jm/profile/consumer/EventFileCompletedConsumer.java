package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.EventFileCompletedEvent;
import com.devision.jm.profile.model.entity.Event;
import com.devision.jm.profile.model.enums.EventStatus;
import com.devision.jm.profile.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event File Completed Consumer
 *
 * Consumes events from File Service when file uploads are completed.
 * Updates the Event entity with file URLs and sets status to ACTIVE.
 *
 * Topic: event-file-completed
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class EventFileCompletedConsumer {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.event-file-completed:event-file-completed}",
            groupId = "${spring.kafka.consumer.group-id:profile-service-group}"
    )
    @Transactional
    public void handleEventFileCompleted(String message) {
        log.info("Received event-file-completed message");

        try {
            EventFileCompletedEvent event = objectMapper.readValue(message, EventFileCompletedEvent.class);
            log.info("Processing file completed event for eventId: {}", event.getEventId());

            // Find the event
            Event existingEvent = eventRepository.findByEventId(event.getEventId())
                    .orElse(null);

            if (existingEvent == null) {
                log.warn("Event not found for eventId: {}. Ignoring message.", event.getEventId());
                return;
            }

            // Check if upload was successful
            if (!event.isSuccess()) {
                log.error("File upload failed for eventId: {}. Error: {}",
                        event.getEventId(), event.getErrorMessage());
                existingEvent.setStatus(EventStatus.FAILED);
                eventRepository.save(existingEvent);
                return;
            }

            // Update event with file URLs
            existingEvent.setCoverImage(event.getCoverImageUrl());
            existingEvent.setCoverImageKey(event.getCoverImageKey());

            if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
                existingEvent.setImageUrls(event.getImageUrls());
                existingEvent.setImageKeys(event.getImageKeys());
            }

            if (event.getVideoUrl() != null) {
                existingEvent.setVideoUrl(event.getVideoUrl());
                existingEvent.setVideoKey(event.getVideoKey());
            }

            // Set status to ACTIVE
            existingEvent.setStatus(EventStatus.ACTIVE);

            eventRepository.save(existingEvent);
            log.info("Event updated with file URLs and set to ACTIVE. eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to process event-file-completed message", e);
            throw new RuntimeException("Failed to process event-file-completed message", e);
        }
    }
}
