package com.devision.jm.profile.service;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.EventApi;
import com.devision.jm.profile.api.internal.dto.EventFileUploadRequest;
import com.devision.jm.profile.model.entity.Event;
import com.devision.jm.profile.model.enums.EventStatus;
import com.devision.jm.profile.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Event Service Implementation
 *
 * Business Logic Layer for Event management.
 * Uses Kafka for async file uploads to File Service.
 *
 * Flow:
 * 1. Create event with PENDING status
 * 2. Send file upload request to File Service via Kafka
 * 3. File Service uploads to S3 and sends back URLs via Kafka
 * 4. EventFileCompletedConsumer updates event with URLs and ACTIVE status
 *
 * Ownership rules:
 * - Only owning company can update/delete event
 * - companyId is resolved from X-User-Id
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EventServiceImpl implements EventApi {

    private final EventRepository eventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.event-file-upload:event-file-upload}")
    private String eventFileUploadTopic;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    // ==================== CREATE WITH FILES ====================
    @Override
    public EventResponse createEventWithFiles(
            String companyId,
            EventCreateRequest request,
            MultipartFile coverImage,
            List<MultipartFile> images,
            MultipartFile video) {

        log.info("Creating event with files for companyId: {}", companyId);

        // Validate cover image is provided
        if (coverImage == null || coverImage.isEmpty()) {
            throw new RuntimeException("Cover image is required");
        }

        // Generate event ID
        String eventId = generateEventId();

        // Create event with PENDING status
        Event event = Event.builder()
                .eventId(eventId)
                .companyId(companyId)
                .title(request.getTitle())
                .caption(request.getCaption())
                .status(EventStatus.PENDING)
                .build();

        Event saved = eventRepository.save(event);
        log.info("Event created with PENDING status. eventId={}", saved.getEventId());

        // Send file upload request to File Service via Kafka
        if (kafkaEnabled) {
            sendFileUploadRequest(eventId, companyId, coverImage, images, video);
        } else {
            log.warn("Kafka is disabled. Files will not be uploaded. eventId={}", eventId);
            // For local testing without Kafka, set to ACTIVE immediately
            saved.setStatus(EventStatus.ACTIVE);
            saved = eventRepository.save(saved);
        }

        return toEventResponse(saved);
    }

    /**
     * Send file upload request to File Service via Kafka
     */
    private void sendFileUploadRequest(
            String eventId,
            String companyId,
            MultipartFile coverImage,
            List<MultipartFile> images,
            MultipartFile video) {

        try {
            EventFileUploadRequest.EventFileUploadRequestBuilder builder = EventFileUploadRequest.builder()
                    .eventId(eventId)
                    .companyId(companyId);

            // Add cover image
            builder.coverImageBase64(encodeToBase64(coverImage))
                    .coverImageFilename(coverImage.getOriginalFilename())
                    .coverImageContentType(coverImage.getContentType());

            // Add additional images if present
            if (images != null && !images.isEmpty()) {
                List<EventFileUploadRequest.FileData> imageDataList = new ArrayList<>();
                for (MultipartFile img : images) {
                    if (img != null && !img.isEmpty()) {
                        imageDataList.add(EventFileUploadRequest.FileData.builder()
                                .base64(encodeToBase64(img))
                                .filename(img.getOriginalFilename())
                                .contentType(img.getContentType())
                                .build());
                    }
                }
                builder.additionalImages(imageDataList);
            }

            // Add video if present
            if (video != null && !video.isEmpty()) {
                builder.videoBase64(encodeToBase64(video))
                        .videoFilename(video.getOriginalFilename())
                        .videoContentType(video.getContentType());
            }

            EventFileUploadRequest request = builder.build();
            String json = objectMapper.writeValueAsString(request);

            kafkaTemplate.send(eventFileUploadTopic, eventId, json);
            log.info("Sent file upload request to Kafka. eventId={}, topic={}", eventId, eventFileUploadTopic);

        } catch (IOException e) {
            log.error("Failed to send file upload request to Kafka. eventId={}", eventId, e);
            // Mark event as FAILED
            eventRepository.findByEventId(eventId).ifPresent(event -> {
                event.setStatus(EventStatus.FAILED);
                eventRepository.save(event);
            });
            throw new RuntimeException("Failed to process file upload request", e);
        }
    }

    private String encodeToBase64(MultipartFile file) throws IOException {
        return Base64.getEncoder().encodeToString(file.getBytes());
    }

    // ==================== READ ====================
    @Override
    public List<EventResponse> getEventsByCompanyId(String companyId) {
        log.info("Fetching all events for companyId: {}", companyId);

        return eventRepository.findByCompanyId(companyId).stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventResponse> getActiveEventsByCompanyId(String companyId) {
        log.info("Fetching active events for companyId: {}", companyId);

        return eventRepository.findByCompanyIdAndStatus(companyId, EventStatus.ACTIVE).stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventByEventId(String eventId) {
        log.info("Fetching event by eventId: {}", eventId);

        Event event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found for eventId: " + eventId));

        return toEventResponse(event);
    }

    // ==================== UPDATE ====================
    @Override
    public EventResponse updateEvent(String companyId, String eventId, EventUpdateRequest request) {
        log.info("Updating event. eventId={}, companyId={}", eventId, companyId);

        Event event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found for eventId: " + eventId));

        // Ownership check
        if (!event.getCompanyId().equals(companyId)) {
            log.warn("Unauthorized update attempt. eventId={}, companyId={}", eventId, companyId);
            throw new RuntimeException("You are not allowed to update this event");
        }

        // Apply partial updates (text fields only, files handled separately)
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getCaption() != null) {
            event.setCaption(request.getCaption());
        }

        Event updated = eventRepository.save(event);
        log.info("Event updated successfully. eventId={}", updated.getEventId());

        return toEventResponse(updated);
    }

    // ==================== DELETE ====================
    @Override
    public void deleteEvent(String companyId, String eventId) {
        log.info("Deleting event. eventId={}, companyId={}", eventId, companyId);

        Event event = eventRepository.findByEventId(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found for eventId: " + eventId));

        // Ownership check
        if (!event.getCompanyId().equals(companyId)) {
            log.warn("Unauthorized delete attempt. eventId={}, companyId={}", eventId, companyId);
            throw new RuntimeException("You are not allowed to delete this event");
        }

        // TODO: Send Kafka message to File Service to delete files from S3
        // For now, just delete the event record

        eventRepository.delete(event);
        log.info("Event deleted successfully. eventId={}", eventId);
    }

    // ==================== HELPER METHODS ====================
    private String generateEventId() {
        return "EVT-" + UUID.randomUUID();
    }

    /**
     * Convert Event entity to EventResponse DTO
     */
    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .companyId(event.getCompanyId())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .title(event.getTitle())
                .caption(event.getCaption())
                .coverImage(event.getCoverImage())
                .imageUrls(event.getImageUrls())
                .videoUrl(event.getVideoUrl())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
