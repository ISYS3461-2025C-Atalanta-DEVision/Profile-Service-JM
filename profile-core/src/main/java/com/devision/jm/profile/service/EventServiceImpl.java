package com.devision.jm.profile.service;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.EventApi;
import com.devision.jm.profile.model.entity.Event;
import com.devision.jm.profile.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Event Service Implementation
 *
 * Business Logic Layer for Event management.
 *
 * Ownership rules: - Only owning company can update/delete event - companyId is
 * resolved from X-User-Id
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class EventServiceImpl implements EventApi {

    private final EventRepository eventRepository;

    // ==================== CREATE ====================
    @Override
    public EventResponse createEvent(String companyId, EventCreateRequest request) {
        log.info("Creating event for companyId: {}", companyId);

        // Business rule validation
        validateMedia(request.getCoverImage());

        Event event = Event.builder()
                .eventId(generateEventId())
                .companyId(companyId)
                .title(request.getTitle())
                .caption(request.getCaption())
                .coverImage(request.getCoverImage())
                .imageUrls(request.getImageUrls())
                .videoUrl(request.getVideoUrl())
                .build();

        Event saved = eventRepository.save(event);
        log.info("Event created successfully. eventId={}", saved.getEventId());

        return toEventResponse(saved);
    }

    // ==================== READ ====================
    @Override
    public List<EventResponse> getEventsByCompanyId(String companyId) {
        log.info("Fetching events for companyId: {}", companyId);

        return eventRepository.findByCompanyId(companyId).stream()
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

        // Apply partial updates
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getCaption() != null) {
            event.setCaption(request.getCaption());
        }
        if (request.getCoverImage() != null) {
            event.setCoverImage(request.getCoverImage());
        }
        if (request.getImageUrls() != null) {
            event.setImageUrls(request.getImageUrls());
        }
        if (request.getVideoUrl() != null) {
            event.setVideoUrl(request.getVideoUrl());
        }

        // Re-validate media after update
        validateMedia(event.getCoverImage());

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

        eventRepository.delete(event);
        log.info("Event deleted successfully. eventId={}", eventId);
    }

    // ==================== HELPER METHODS ====================
    private String generateEventId() {
        return "EVT-" + UUID.randomUUID();
    }

    /**
     * Media validation rules
     */
    private void validateMedia(String coverImage) {

        if (coverImage == null || coverImage.isBlank()) {
            throw new RuntimeException("Cover image is required");
        }

    }

    /**
     * Convert Event entity to EventResponse DTO
     */
    private EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .eventId(event.getEventId())
                .companyId(event.getCompanyId())
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
