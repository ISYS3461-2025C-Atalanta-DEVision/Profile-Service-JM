package com.devision.jm.profile.model.embedded;

import com.devision.jm.profile.model.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Media Item Embedded Document
 *
 * Represents a single media item (image or video) in the company gallery.
 *
 * Implements requirements:
 * - 3.2.2: Company Gallery with images and videos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem {

    @Field("media_type")
    private MediaType mediaType;

    @Field("url")
    private String url;

    @Field("thumbnail_url")
    private String thumbnailUrl;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("display_order")
    private Integer displayOrder;

    @Field("uploaded_at")
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
