package com.devision.jm.profile.model.entity;

import com.devision.jm.profile.model.enums.EventStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;


/**
 * Event Document (MongoDB)
 *
 * Represents a company-created event linked to a Profile (company).
 *
 * Ownership: - Profile Service owns event content and metadata
 *
 * Relationships: - companyId links to Profile.userId
 *
 * Supports: - Text-based events - Image or video-based events (optional media)
 */
@Document(collection = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Event extends BaseEntity {

    // ==================== Company Reference ====================
    /**
     * Event ID
     */
    @Indexed(unique = true)
    @Field("event_id")
    private String eventId;

    /**
     * Company ID (Profile.userId) Used to associate event with a company
     * profile
     */
    @Indexed
    @Field("company_id")
    private String companyId;

    // ==================== Event Status ====================
    /**
     * Event status (PENDING, ACTIVE, FAILED)
     */
    @Field("status")
    @Builder.Default
    private EventStatus status = EventStatus.PENDING;

    // ==================== Event Content ====================
    /**
     * Event title
     */
    @Field("title")
    private String title;

    /**
     * Short description / caption for the event
     */
    @Field("caption")
    private String caption;

    // ==================== Media ====================
    /**
     * Cover image URL (used for listing / preview)
     */
    @Field("cover_image")
    private String coverImage;

    /**
     * S3 key for cover image (for deletion)
     */
    @Field("cover_image_key")
    private String coverImageKey;

    /**
     * Image gallery URLs (optional)
     */
    @Field("image_urls")
    private List<String> imageUrls;

    /**
     * S3 keys for image gallery (for deletion)
     */
    @Field("image_keys")
    private List<String> imageKeys;

    /**
     * Video URL (optional)
     */
    @Field("video_url")
    private String videoUrl;

    /**
     * S3 key for video (for deletion)
     */
    @Field("video_key")
    private String videoKey;
}
