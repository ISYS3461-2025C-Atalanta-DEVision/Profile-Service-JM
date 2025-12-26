package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Avatar File Upload Request (Internal Kafka Event)
 *
 * Sent from Profile Service to File Service via Kafka
 * to request avatar upload for a user profile.
 *
 * Topic: avatar-file-upload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarFileUploadRequest {

    /**
     * User ID (profile owner)
     */
    private String userId;

    /**
     * Avatar image as base64 encoded string
     */
    private String avatarBase64;

    /**
     * Avatar image original filename
     */
    private String avatarFilename;

    /**
     * Avatar image content type (e.g., image/jpeg)
     */
    private String avatarContentType;
}
