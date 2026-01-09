package com.devision.jm.profile.controller;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.EventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Event Controller
 *
 * REST controller for company events. Implements the Presentation Layer (A.1.1,
 * A.2.2).
 *
 * Ownership: - Profile Service owns company-created events
 *
 * Identity: - companyId is resolved from X-User-Id header (Profile.userId)
 *
 * Endpoints:
 * - POST /api/events - Create event with files (multipart)
 * - GET /api/events/me - Get my company events
 * - GET /api/events/{companyId} - Get events by company ID
 * - PUT /api/events/{eventId} - Update event
 * - DELETE /api/events/{eventId} - Delete event
 */
@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventApi eventService;

    /**
     * Create a new event with file uploads
     * POST /api/events (multipart/form-data)
     *
     * @param companyId Company ID from X-User-Id header
     * @param title Event title
     * @param caption Event caption/description
     * @param coverImage Cover image file (required)
     * @param images Additional image files (optional, max 10)
     * @param video Video file (optional)
     * @return EventResponse with status PENDING (files processing)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvent(
            @RequestHeader("X-User-Id") String companyId,
            @RequestParam("title") String title,
            @RequestParam("caption") String caption,
            @RequestParam("coverImage") MultipartFile coverImage,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "video", required = false) MultipartFile video) {

        log.info("Create event request for companyId: {}, title: {}", companyId, title);

        // Build the request DTO
        EventCreateRequest request = EventCreateRequest.builder()
                .title(title)
                .caption(caption)
                .build();

        EventResponse response = eventService.createEventWithFiles(
                companyId, request, coverImage, images, video);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all events created by current company
     * GET /api/events/me
     *
     * Only returns ACTIVE events by default
     */
    @GetMapping("/me")
    public ResponseEntity<List<EventResponse>> getMyEvents(
            @RequestHeader("X-User-Id") String companyId,
            @RequestParam(value = "includeAll", defaultValue = "false") boolean includeAll) {

        log.info("Get events request for companyId: {}, includeAll: {}", companyId, includeAll);
        List<EventResponse> response = includeAll
                ? eventService.getEventsByCompanyId(companyId)
                : eventService.getActiveEventsByCompanyId(companyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a single event by event ID
     * GET /api/events/event/{eventId}
     *
     * Returns the event regardless of status (for owner viewing)
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<EventResponse> getEventByEventId(
            @PathVariable String eventId) {

        log.info("Get event by eventId: {}", eventId);
        EventResponse response = eventService.getEventByEventId(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get events by company ID
     * GET /api/events/company/{companyId}
     *
     * Used by external services (e.g. Job Applicant team)
     * Only returns ACTIVE events
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<EventResponse>> getEventsByCompanyId(
            @PathVariable String companyId) {

        log.info("Get events by companyId: {}", companyId);
        List<EventResponse> response = eventService.getActiveEventsByCompanyId(companyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing event
     * PUT /api/events/{eventId}
     */
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @RequestHeader("X-User-Id") String companyId,
            @PathVariable String eventId,
            @Valid @RequestBody EventUpdateRequest request) {

        log.info("Update event request for eventId: {}, companyId: {}", eventId, companyId);
        EventResponse response = eventService.updateEvent(companyId, eventId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an event
     * DELETE /api/events/{eventId}
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @RequestHeader("X-User-Id") String companyId,
            @PathVariable String eventId) {

        log.info("Delete event request for eventId: {}, companyId: {}", eventId, companyId);
        eventService.deleteEvent(companyId, eventId);
        return ResponseEntity.noContent().build();
    }
}
