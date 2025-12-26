package com.devision.jm.profile.api.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event Response DTO (External)
 *
 * Response for event data endpoints.
 * Implements A.2.5: Only presents necessary data.
 *
 * Used by:
 * - GET /api/events/me
 * - GET /api/events/company/{companyId}
 * - GET /api/events/{eventId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    // ==================== Identifiers ====================

    /**
     * MongoDB document ID
     */
    private String id;

    /**
     * Business event ID
     */
    private String eventId;

    /**
     * Company ID (Profile.userId)
     */
    private String companyId;

    // ==================== Event Content ====================

    /**
     * Event title
     */
    private String title;

    /**
     * Event caption / description
     */
    private String caption;

    // ==================== Media ====================

    /**
     * Cover image URL (used for preview / listing)
     */
    private String coverImage;

    /**
     * Image gallery URLs (optional)
     */
    private List<String> imageUrls;

    /**
     * Video URL (optional)
     */
    private String videoUrl;

    // ==================== Metadata ====================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
