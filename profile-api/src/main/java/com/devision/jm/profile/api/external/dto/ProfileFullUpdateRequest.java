package com.devision.jm.profile.api.external.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile Full Update Request DTO (External)
 *
 * Request for fully updating all profile data.
 * Replaces ALL fields (null values will clear the field).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileFullUpdateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must be less than 255 characters")
    private String companyName;

    @Pattern(
            regexp = "^$|^\\+[1-9]\\d{0,2}\\d{1,12}$",
            message = "Phone number must start with a valid dial code (e.g., +84) followed by up to 12 digits"
    )
    private String phoneNumber;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 255, message = "Street address must be less than 255 characters")
    private String streetAddress;

    // ==================== Public Profile (3.1.2) ====================

    /**
     * About Us - Company description
     */
    @Size(max = 5000, message = "About Us must be less than 5000 characters")
    private String aboutUs;

    /**
     * Who We Are Looking For - Desired employee personalities
     */
    @Size(max = 5000, message = "Who We Are Looking For must be less than 5000 characters")
    private String whoWeAreLookingFor;
}
