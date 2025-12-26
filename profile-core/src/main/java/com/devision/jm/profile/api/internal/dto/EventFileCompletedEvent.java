package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event File Completed Event (Internal Kafka Event)
 *
 * Sent from File Service to Profile Service via Kafka
 * when file uploads are completed for an event.
 *
 * Topic: event-file-completed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFileCompletedEvent {

    /**
     * Event ID the files belong to
     */
    private String eventId;

    /**
     * Company ID (owner of the event)
     */
    private String companyId;

    /**
     * Whether the upload was successful
     */
    private boolean success;

    /**
     * Error message if upload failed
     */
    private String errorMessage;

    /**
     * CDN URL for cover image
     */
    private String coverImageUrl;

    /**
     * S3 key for cover image (for deletion)
     */
    private String coverImageKey;

    /**
     * CDN URLs for additional images
     */
    private List<String> imageUrls;

    /**
     * S3 keys for additional images (for deletion)
     */
    private List<String> imageKeys;

    /**
     * CDN URL for video (optional)
     */
    private String videoUrl;

    /**
     * S3 key for video (for deletion)
     */
    private String videoKey;
}
