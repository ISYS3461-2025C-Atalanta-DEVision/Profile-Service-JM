package com.devision.jm.profile.api.external.interfaces;

import com.devision.jm.profile.api.external.dto.ProfileCreateRequest;
import com.devision.jm.profile.api.external.dto.ProfileFullUpdateRequest;
import com.devision.jm.profile.api.external.dto.ProfileResponse;
import com.devision.jm.profile.api.external.dto.ProfileUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Profile API Interface (External)
 *
 * Defines external API contract for profile operations.
 * These APIs are accessible by other modules/services (A.2.3, A.2.4).
 *
 * Microservice Architecture (A.3.1):
 * - Profile Service handles: user profiles, company info, subscriptions
 * - Auth Service handles: authentication, tokens
 */
public interface ProfileApi {

    /**
     * Get profile by userId (from Auth Service)
     *
     * @param userId User ID from Auth Service
     * @return Profile response
     */
    ProfileResponse getProfileByUserId(String userId);

    /**
     * Get profile by profile ID
     * Used for external API access (e.g., Job Applicant team)
     *
     * @param profileId Profile ID (MongoDB _id)
     * @return Profile response
     */
    ProfileResponse getProfileById(String profileId);

    /**
     * Get profile by email
     *
     * @param email User's email
     * @return Profile response
     */
    ProfileResponse getProfileByEmail(String email);

    /**
     * Update profile
     *
     * @param userId User ID from Auth Service
     * @param request Update request with new data
     * @return Updated profile response
     */
    ProfileResponse updateProfile(String userId, ProfileUpdateRequest request);

    /**
     * Search profiles (for admin)
     *
     * @param searchTerm Search term (email or company name)
     * @return List of matching profiles
     */
    List<ProfileResponse> searchProfiles(String searchTerm);

    /**
     * Create profile for existing user
     * Used for users who registered before Kafka was enabled
     *
     * @param request Profile creation request
     * @return Created profile response
     */
    ProfileResponse createProfile(ProfileCreateRequest request);

    /**
     * Full update profile - replaces all editable fields
     *
     * @param userId User ID from Auth Service
     * @param request Full update request with all fields
     * @return Updated profile response
     */
    ProfileResponse fullUpdateProfile(String userId, ProfileFullUpdateRequest request);

    /**
     * Upload avatar for user profile
     * Sends file to File Service via Kafka for async processing.
     *
     * @param userId User ID from Auth Service
     * @param avatar Avatar image file
     * @return Profile response (avatar will be updated async)
     */
    ProfileResponse uploadAvatar(String userId, MultipartFile avatar);
}
