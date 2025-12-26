package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Avatar File Completed Event (Internal Kafka Event)
 *
 * Sent from File Service to Profile Service via Kafka
 * after avatar upload is completed.
 *
 * Topic: avatar-file-completed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarFileCompletedEvent {

    /**
     * User ID (profile owner)
     */
    private String userId;

    /**
     * CDN URL of the uploaded avatar
     */
    private String avatarUrl;

    /**
     * S3 key of the uploaded avatar
     */
    private String avatarKey;

    /**
     * Whether the upload was successful
     */
    private boolean success;

    /**
     * Error message if upload failed
     */
    private String errorMessage;
}
