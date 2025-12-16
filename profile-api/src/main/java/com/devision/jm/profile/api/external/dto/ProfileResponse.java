package com.devision.jm.profile.api.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Profile Response DTO (External)
 *
 * Response for profile data endpoints.
 * Implements A.2.5: Only presents necessary data (excludes sensitive info).
 *
 * Used by:
 * - GET /api/profile/{userId}
 * - GET /api/profile/me (authenticated user's profile)
 *
 * Implements requirements:
 * - 3.1.2: About Us, Who We Are Looking For
 * - 3.2.1: Company Logo
 * - 6.1.1: Subscription info
 * - 6.2.1-6.2.4: Applicant Search Profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private String id;
    private String userId;
    private String email;

    // ==================== Basic Company Info ====================

    private String companyName;
    private String avatarUrl;

    /**
     * 3.2.1: Company Logo URL
     */
    private String logoUrl;

    /**
     * 3.1.2: About Us
     */
    private String aboutUs;

    /**
     * 3.1.2: Who We Are Looking For
     */
    private String whoWeAreLookingFor;

    // ==================== Location Info ====================

    private String country;
    private String city;
    private String streetAddress;

    // ==================== Contact Info ====================

    private String phoneNumber;

    // ==================== Subscription Info (6.1.1) ====================

    private String subscriptionType;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private Long daysRemaining;
    private Boolean subscriptionActive;

    // ==================== Applicant Search Profile (6.2.1-6.2.4) ====================

    /**
     * 6.2.1-6.2.4: Applicant Search Criteria
     */
    private ApplicantSearchProfileDto applicantSearchProfile;

    // ==================== Metadata ====================

    private String authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
