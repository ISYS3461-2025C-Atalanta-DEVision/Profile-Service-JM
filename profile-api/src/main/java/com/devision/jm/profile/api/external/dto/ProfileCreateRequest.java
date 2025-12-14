package com.devision.jm.profile.api.external.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile Create Request DTO
 *
 * Used to manually create profiles for existing users
 * who registered before Kafka was enabled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreateRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String companyName;
    private String country;
    private String city;
    private String streetAddress;
    private String phoneNumber;
    private String avatarUrl;
    private String authProvider;
}
