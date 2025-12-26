package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.internal.dto.AvatarFileCompletedEvent;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Avatar File Completed Consumer
 *
 * Consumes avatar upload completion events from File Service via Kafka.
 * Updates the profile with the new avatar URL.
 *
 * Topic: avatar-file-completed
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class AvatarFileCompletedConsumer {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.avatar-file-completed:avatar-file-completed}",
            groupId = "${spring.kafka.consumer.group-id:profile-service-group}"
    )
    public void handleAvatarFileCompleted(String message) {
        log.info("Received avatar-file-completed message");

        try {
            AvatarFileCompletedEvent event = objectMapper.readValue(message, AvatarFileCompletedEvent.class);
            log.info("Processing avatar completion for userId: {}, success: {}",
                    event.getUserId(), event.isSuccess());

            Profile profile = profileRepository.findByUserId(event.getUserId())
                    .orElse(null);

            if (profile == null) {
                log.error("Profile not found for userId: {}", event.getUserId());
                return;
            }

            if (event.isSuccess()) {
                // Update profile with new avatar URL
                profile.setAvatarUrl(event.getAvatarUrl());
                profileRepository.save(profile);
                log.info("Profile avatar updated successfully. userId={}, avatarUrl={}",
                        event.getUserId(), event.getAvatarUrl());
            } else {
                log.error("Avatar upload failed for userId: {}. Error: {}",
                        event.getUserId(), event.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Failed to process avatar-file-completed message", e);
        }
    }
}
