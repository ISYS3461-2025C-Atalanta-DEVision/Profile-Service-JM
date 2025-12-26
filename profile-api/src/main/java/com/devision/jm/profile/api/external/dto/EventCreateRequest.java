package com.devision.jm.profile.api.external.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event Create Request DTO
 *
 * Used to create a new event for a company.
 *
 * Ownership:
 * - companyId is resolved from X-User-Id header
 * - MUST NOT be provided by client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateRequest {

    /**
     * Event title
     */
    @NotBlank(message = "Event title is required")
    @Size(max = 150, message = "Event title must not exceed 150 characters")
    private String title;

    /**
     * Event caption / description
     */
    @NotBlank(message = "Event caption is required")
    @Size(max = 1000, message = "Event caption must not exceed 1000 characters")
    private String caption;

    /**
     * Cover image URL (used for preview / listing)
     */
    @NotBlank(message = "Cover image is required")
    @Size(max = 500, message = "Cover image URL must not exceed 500 characters")
    private String coverImage;

    /**
     * Image gallery URLs (optional)
     */
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String
    > imageUrls;

    /**
     * Video URL (optional)
     */
    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoUrl;
}