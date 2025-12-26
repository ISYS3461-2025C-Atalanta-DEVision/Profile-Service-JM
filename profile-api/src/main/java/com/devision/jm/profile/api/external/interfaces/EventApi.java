package com.devision.jm.profile.api.external.interfaces;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;

import java.util.List;

/**
 * Event API Interface (External)
 *
 * Defines external API contract for event (post) operations.
 * These APIs are accessible by other modules/services.
 *
 * Ownership:
 * - Profile Service owns event content and metadata
 * - companyId links to Profile.userId
 */
public interface EventApi {

    /**
     * Create a new event
     *
     * @param companyId Company ID (from X-User-Id header)
     * @param request Event creation request
     * @return Created event response
     */
    EventResponse createEvent(String companyId, EventCreateRequest request);

    /**
     * Get all events by company ID
     *
     * @param companyId Company ID
     * @return List of events for the company
     */
    List<EventResponse> getEventsByCompanyId(String companyId);

    /**
     * Get event by event ID
     *
     * @param eventId Event ID
     * @return Event response
     */
    EventResponse getEventByEventId(String eventId);

    /**
     * Update an existing event
     *
     * @param companyId Company ID (for ownership check)
     * @param eventId Event ID to update
     * @param request Update request with new data
     * @return Updated event response
     */
    EventResponse updateEvent(String companyId, String eventId, EventUpdateRequest request);

    /**
     * Delete an event
     *
     * @param companyId Company ID (for ownership check)
     * @param eventId Event ID to delete
     */
    void deleteEvent(String companyId, String eventId);
}
