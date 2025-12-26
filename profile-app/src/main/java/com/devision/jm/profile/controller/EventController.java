package com.devision.jm.profile.controller;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.EventApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 * Endpoints: - POST /api/events - Create event - GET /api/events/me - Get my
 * company events - PUT /api/events/{eventId} - Update event - DELETE
 * /api/events/{eventId} - Delete event
 */
@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventApi eventService;

    /**
     * Create a new event POST /api/events
     */
    @PostMapping()
    public ResponseEntity<EventResponse> createEvent(
            @RequestHeader("X-User-Id") String companyId,
            @Valid @RequestBody EventCreateRequest request) {

        log.info("Create event request for companyId: {}", companyId);
        EventResponse response = eventService.createEvent(companyId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all events created by current company GET /api/events/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<EventResponse>> getMyEvents(
            @RequestHeader("X-User-Id") String companyId) {

        log.info("Get events request for companyId: {}", companyId);
        List<EventResponse> response = eventService.getEventsByCompanyId(companyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get events by company ID GET /api/events/{companyId}
     *
     * Used by external services (e.g. Job Applicant team)
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<List<EventResponse>> getEventsByCompanyId(
            @PathVariable String companyId) {

        log.info("Get events by companyId: {}", companyId);
        List<EventResponse> response = eventService.getEventsByCompanyId(companyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing event PUT /api/events/{eventId}
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
     * Delete an event DELETE /api/events/{eventId}
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
