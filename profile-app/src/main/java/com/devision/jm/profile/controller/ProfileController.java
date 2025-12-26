package com.devision.jm.profile.controller;

import com.devision.jm.profile.api.external.dto.ProfileFullUpdateRequest;
import com.devision.jm.profile.api.external.dto.ProfileResponse;
import com.devision.jm.profile.api.external.interfaces.ProfileApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Profile Controller
 *
 * REST controller for profile endpoints.
 * Implements the Presentation Layer (A.1.1, A.2.2).
 *
 * Microservice Architecture (A.3.1):
 * - Profile Service handles: user profiles, company info
 * - Auth Service handles: authentication
 *
 * Note: userId is passed via header from API Gateway after token validation
 * Note: Profile creation is handled via Kafka events from Auth Service
 *
 * Endpoints:
 * - GET  /api/profiles/me          - Get current user's profile
 * - GET  /api/profiles/{userId}    - Get profile by user ID (for job posts)
 * - GET  /api/profiles?search=     - Search profiles by email or company name
 * - PUT  /api/profiles/me          - Update current user's profile
 */
@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileApi profileService;

    /**
     * Get current user's profile
     * GET /api/profiles/me
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Get profile request for userId: {}", userId);
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile by user ID
     * GET /api/profiles/{userId}
     * Used by frontend to fetch company info for job posts
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(
            @PathVariable("userId") String userId) {
        log.info("Get profile by userId: {}", userId);
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Search profiles by email or company name
     * GET /api/profiles?search={searchTerm}
     */
    @GetMapping
    public ResponseEntity<List<ProfileResponse>> searchProfiles(
            @RequestParam("search") String searchTerm) {
        log.info("Search profiles request with term: {}", searchTerm);
        List<ProfileResponse> response = profileService.searchProfiles(searchTerm);
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user's profile
     * PUT /api/profiles/me
     */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProfileFullUpdateRequest request) {
        log.info("Update profile request for userId: {}", userId);
        ProfileResponse response = profileService.fullUpdateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload avatar for current user's profile
     * POST /api/profiles/me/avatar (multipart/form-data)
     *
     * Avatar is sent to File Service via Kafka for async processing.
     * The avatar URL will be updated in the profile when processing completes.
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("avatar") MultipartFile avatar) {
        log.info("Upload avatar request for userId: {}", userId);
        ProfileResponse response = profileService.uploadAvatar(userId, avatar);
        return ResponseEntity.ok(response);
    }
}
