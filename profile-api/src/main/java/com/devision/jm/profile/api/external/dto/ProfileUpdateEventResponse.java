package com.devision.jm.profile.api.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Profile Update Event DTO (External)
 *
 * This DTO is used as the payload for profile update events published
 * from the Company Profile microservice (e.g. via Kafka).
 *
 * Purpose:
 * - Carries only non‑sensitive company profile data needed by other services.
 * - Enables downstream services (e.g. Job Post, Notification) to keep a local
 *   cache of company information such as name, logo, and location.
 *
 * Fields:
 * - userId:        Identifier of the company (companyId in other contexts).
 * - companyName:   Public display name of the company.
 * - avatarUrl:     URL of the company avatar (optional).
 * - logoUrl:       URL of the company logo (implements requirement 3.2.1).
 * - country/city/streetAddress: Location information for the company.
 * - phoneNumber:   Public contact phone number.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateEventResponse {

    private String userId; //companyId

    // ==================== Basic Company Info ====================

    private String companyName;
    private String avatarUrl;

    /**
     * 3.2.1: Company Logo URL
     */
    private String logoUrl;

    // ==================== Location Info ====================

    private String country;
    private String city;
    private String streetAddress;

    // ==================== Contact Info ====================

    private String phoneNumber;

}
