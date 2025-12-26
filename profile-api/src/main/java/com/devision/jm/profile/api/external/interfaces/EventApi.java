package com.devision.jm.profile.api.external.interfaces;

import com.devision.jm.profile.api.external.dto.EventCreateRequest;
import com.devision.jm.profile.api.external.dto.EventResponse;
import com.devision.jm.profile.api.external.dto.EventUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

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
     * Create a new event with file uploads
     * Files are sent to File Service via Kafka for async processing.
     *
     * @param companyId Company ID (from X-User-Id header)
     * @param request Event creation request (title, caption)
     * @param coverImage Cover image file (required)
     * @param images Additional image files (optional)
     * @param video Video file (optional)
     * @return Created event response with status PENDING
     */
    EventResponse createEventWithFiles(
            String companyId,
            EventCreateRequest request,
            MultipartFile coverImage,
            List<MultipartFile> images,
            MultipartFile video);

    /**
     * Get all events by company ID (all statuses)
     *
     * @param companyId Company ID
     * @return List of all events for the company
     */
    List<EventResponse> getEventsByCompanyId(String companyId);

    /**
     * Get only ACTIVE events by company ID
     *
     * @param companyId Company ID
     * @return List of active events for the company
     */
    List<EventResponse> getActiveEventsByCompanyId(String companyId);

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
