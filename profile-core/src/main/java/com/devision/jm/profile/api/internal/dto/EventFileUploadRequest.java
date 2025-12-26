package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event File Upload Request (Internal Kafka Event)
 *
 * Sent from Profile Service to File Service via Kafka
 * to request file uploads for an event.
 *
 * Topic: event-file-upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFileUploadRequest {

    /**
     * Event ID to associate files with
     */
    private String eventId;

    /**
     * Company ID (owner of the event)
     */
    private String companyId;

    /**
     * Cover image as base64 encoded string
     */
    private String coverImageBase64;

    /**
     * Cover image original filename
     */
    private String coverImageFilename;

    /**
     * Cover image content type (e.g., image/jpeg)
     */
    private String coverImageContentType;

    /**
     * Additional images as base64 encoded strings
     */
    private List<FileData> additionalImages;

    /**
     * Video as base64 encoded string (optional)
     */
    private String videoBase64;

    /**
     * Video original filename
     */
    private String videoFilename;

    /**
     * Video content type (e.g., video/mp4)
     */
    private String videoContentType;

    /**
     * Nested class for additional image data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileData {
        private String base64;
        private String filename;
        private String contentType;
    }
}
