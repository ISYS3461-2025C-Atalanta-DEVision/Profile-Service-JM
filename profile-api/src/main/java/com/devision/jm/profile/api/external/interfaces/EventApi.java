package com.devision.jm.profile.api.external.interfaces;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;

import java.util.List;

/**
 * Event API Interface (External)
 *
 * Defines external API contract for event operations.
 * These APIs are accessible by other modules/services (A.2.3, A.2.4).
 *
 * Microservice Architecture (A.3.1):
 * - Profile Service owns: company-created events
 * - Auth Service handles: authentication, tokens
 */
public interface EventApi {

    /**
     * Create a new event
     *
     * @param companyId Company ID (Profile.userId)
     * @param request Event creation request
     * @return Created event response
     */
    EventResponse createEvent(String companyId, EventCreateRequest request);

    /**
     * Get all events created by a company
     *
     * Used by:
     * - Company dashboard (GET /api/events/me)
     * - External services (GET /api/events/company/{companyId})
     *
     * @param companyId Company ID (Profile.userId)
     * @return List of events
     */
    List<EventResponse> getEventsByCompanyId(String companyId);

    /**
     * Get event by eventId
     *
     * Used for public event detail pages
     *
     * @param eventId Event business ID
     * @return Event response
     */
    EventResponse getEventByEventId(String eventId);

    /**
     * Update an existing event
     *
     * Ownership check required:
     * - event.companyId must match provided companyId
     *
     * @param companyId Company ID (Profile.userId)
     * @param eventId Event business ID
     * @param request Update request
     * @return Updated event response
     */
    EventResponse updateEvent(String companyId, String eventId, EventUpdateRequest request);

    /**
     * Delete an event
     *
     * Ownership check required:
     * - event.companyId must match provided companyId
     *
     * @param companyId Company ID (Profile.userId)
     * @param eventId Event business ID
     */
    void deleteEvent(String companyId, String eventId);
}
