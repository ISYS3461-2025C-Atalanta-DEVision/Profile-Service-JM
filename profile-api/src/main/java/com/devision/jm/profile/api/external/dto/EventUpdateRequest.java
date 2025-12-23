package com.devision.jm.profile.api.external.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event Update Request DTO (External)
 *
 * Request for updating event data.
 * All fields are optional – only provided fields will be updated.
 *
 * Ownership:
 * - companyId resolved from X-User-Id
 * - eventId from path variable
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequest {

    // ==================== Event Content ====================

    /**
     * Event title
     */
    @Size(max = 150, message = "Event title must not exceed 150 characters")
    private String title;

    /**
     * Event caption / description
     */
    @Size(max = 1000, message = "Event caption must not exceed 1000 characters")
    private String caption;

    // ==================== Media ====================

    /**
     * Cover image URL
     */
    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImage;

    /**
     * Image gallery URLs
     */
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String
    > imageUrls;

    /**
     * Video URL
     */
    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoUrl;
}
