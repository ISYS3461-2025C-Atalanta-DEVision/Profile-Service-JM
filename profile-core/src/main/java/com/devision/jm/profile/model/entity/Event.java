package com.devision.jm.profile.model.entity;

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
     * Image gallery URLs (optional)
     */
    @Field("image_urls")
    private List<String> imageUrls;

    /**
     * Video URL (optional)
     */
    @Field("video_url")
    private String videoUrl;
}
