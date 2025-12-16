package com.devision.jm.profile.model.entity;

import com.devision.jm.profile.model.embedded.ApplicantSearchProfile;
import com.devision.jm.profile.model.enums.SubscriptionType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Profile Document (MongoDB) - User Profile Data
 *
 * This entity contains profile-related fields that were separated from Auth Service.
 *
 * Microservice Architecture (A.3.1, A.3.3):
 * - Auth Service owns: email, password, tokens, security fields
 * - Profile Service owns: company info, contact info, subscription
 *
 * Data Source:
 * - Created from UserCreatedEvent consumed from Kafka (A.3.2)
 * - userId links to Auth Service's User entity
 *
 * Implements requirements:
 * - 1.1.1: Company registration profile data
 * - 1.3.3: Sharding by country
 * - 3.1.2: About Us, Who We Are Looking For
 * - 3.2.1: Company Logo
 * - 6.1.1: Subscription management
 * - 6.2.1-6.2.4: Applicant Searching Profile
 */
@Document(collection = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Profile extends BaseEntity {

    // ==================== Reference to Auth Service ====================

    /**
     * User ID from Auth Service (MongoDB ObjectId as String)
     * This is the foreign key linking to Auth Service's User entity.
     * Indexed for fast lookups.
     */
    @Indexed(unique = true)
    @Field("user_id")
    private String userId;

    /**
     * Email (copied from Auth Service for display)
     * Note: Auth Service is the source of truth for email
     */
    @Indexed
    @Field("email")
    private String email;

    // ==================== Company Information ====================

    private static final String DEFAULT_AVATAR_URL = "default";

    @Field("company_name")
    private String companyName;

    @Field("avatar_url")
    @Builder.Default
    private String avatarUrl = DEFAULT_AVATAR_URL;

    /**
     * 3.2.1: Company Logo URL
     * Separate from avatarUrl for profile picture vs company branding
     */
    @Field("logo_url")
    private String logoUrl;

    /**
     * 3.1.2: About Us
     * Company description/introduction text
     */
    @Field("about_us")
    private String aboutUs;

    /**
     * 3.1.2: Who We Are Looking For
     * Description of ideal candidates the company is seeking
     */
    @Field("who_we_are_looking_for")
    private String whoWeAreLookingFor;

    // ==================== Location Information (1.3.3 Sharding) ====================

    /**
     * Country - Used for sharding/partitioning (1.3.3)
     */
    @Indexed
    @Field("country")
    private String country;

    @Field("city")
    private String city;

    @Field("street_address")
    private String streetAddress;

    // ==================== Contact Information ====================

    @Field("phone_number")
    private String phoneNumber;

    // ==================== Subscription (6.1.1) ====================

    @Field("subscription_type")
    @Builder.Default
    private SubscriptionType subscriptionType = SubscriptionType.FREE;

    @Field("subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Field("subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    /**
     * 6.1.2: Track if 7-day expiry notification was sent
     */
    @Field("expiry_notification_sent")
    @Builder.Default
    private Boolean expiryNotificationSent = false;

    /**
     * 6.1.2: Track if expiry notification was sent
     */
    @Field("expired_notification_sent")
    @Builder.Default
    private Boolean expiredNotificationSent = false;

    // ==================== Applicant Searching Profile (6.2.1-6.2.4) ====================

    /**
     * 6.2.1-6.2.4: Applicant Search Criteria (Premium Feature)
     * Contains desired skills, employment status, country, salary range, education
     */
    @Field("applicant_search_profile")
    private ApplicantSearchProfile applicantSearchProfile;

    // ==================== Auth Provider (for display) ====================

    @Field("auth_provider")
    private String authProvider;

    // ==================== Helper Methods ====================

    /**
     * Check if subscription is active (only for PREMIUM users)
     */
    public boolean isSubscriptionActive() {
        if (subscriptionType != SubscriptionType.PREMIUM) {
            return false;  // FREE users don't have an active subscription
        }
        if (subscriptionEndDate == null) {
            return true;  // PREMIUM with no end date is active
        }
        return LocalDateTime.now().isBefore(subscriptionEndDate);
    }

    /**
     * Get days remaining in subscription (only for PREMIUM users)
     * Returns null for FREE users
     */
    public Long getDaysRemaining() {
        if (subscriptionType != SubscriptionType.PREMIUM || subscriptionEndDate == null) {
            return null;  // No days remaining for FREE users
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), subscriptionEndDate);
        return Math.max(0, days);
    }

    /**
     * Check if this is a premium subscriber
     */
    public boolean isPremiumSubscriber() {
        return subscriptionType == SubscriptionType.PREMIUM && isSubscriptionActive();
    }
}
