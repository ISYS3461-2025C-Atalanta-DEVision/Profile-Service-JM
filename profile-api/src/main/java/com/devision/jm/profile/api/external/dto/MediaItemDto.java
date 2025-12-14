package com.devision.jm.profile.api.external.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Media Item DTO
 *
 * DTO for media gallery items (images/videos).
 *
 * Implements requirements:
 * - 3.2.2: Company Gallery
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItemDto {

    /**
     * Type of media: IMAGE or VIDEO
     */
    @NotBlank(message = "Media type is required")
    private String mediaType;

    /**
     * URL of the media file
     */
    @NotBlank(message = "URL is required")
    @Size(max = 1000, message = "URL must be less than 1000 characters")
    private String url;

    /**
     * Thumbnail URL (especially for videos)
     */
    @Size(max = 1000, message = "Thumbnail URL must be less than 1000 characters")
    private String thumbnailUrl;

    /**
     * Title/caption for the media
     */
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    /**
     * Description of the media
     */
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    /**
     * Display order in gallery (lower = first)
     */
    private Integer displayOrder;

    /**
     * When the media was uploaded
     */
    private LocalDateTime uploadedAt;
}
