package com.devision.jm.profile.api.external.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Profile Update Request DTO (External)
 *
 * Request for updating profile data.
 * All fields are optional - only provided fields will be updated.
 *
 * Implements requirements:
 * - 1.2.3: Phone number validation (optional)
 * - 3.1.2: About Us, Who We Are Looking For
 * - 3.2.1: Company Logo
 * - 3.2.2: Media Gallery
 * - 6.2.1-6.2.4: Applicant Search Profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    // ==================== Basic Company Info ====================

    @Size(max = 255, message = "Company name must be less than 255 characters")
    private String companyName;

    @Size(max = 500, message = "Avatar URL must be less than 500 characters")
    private String avatarUrl;

    /**
     * 3.2.1: Company Logo URL
     */
    @Size(max = 500, message = "Logo URL must be less than 500 characters")
    private String logoUrl;

    /**
     * 3.1.2: About Us - Company description
     */
    @Size(max = 5000, message = "About Us must be less than 5000 characters")
    private String aboutUs;

    /**
     * 3.1.2: Who We Are Looking For - Ideal candidate description
     */
    @Size(max = 3000, message = "Who We Are Looking For must be less than 3000 characters")
    private String whoWeAreLookingFor;

    // ==================== Location Info ====================

    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 255, message = "Street address must be less than 255 characters")
    private String streetAddress;

    // ==================== Contact Info ====================

    @Pattern(
            regexp = "^$|^\\+[1-9]\\d{0,2}\\d{1,12}$",
            message = "Phone number must start with a valid dial code (e.g., +84) followed by up to 12 digits"
    )
    private String phoneNumber;

    // ==================== Media Gallery (3.2.2) ====================

    /**
     * 3.2.2: Media Gallery - List of images/videos
     * When provided, replaces the entire gallery
     */
    @Valid
    @Size(max = 20, message = "Media gallery can contain maximum 20 items")
    private List<MediaItemDto> mediaGallery;

    // ==================== Applicant Search Profile (6.2.1-6.2.4) ====================

    /**
     * 6.2.1-6.2.4: Applicant Search Criteria (Premium Feature)
     */
    @Valid
    private ApplicantSearchProfileDto applicantSearchProfile;
}
