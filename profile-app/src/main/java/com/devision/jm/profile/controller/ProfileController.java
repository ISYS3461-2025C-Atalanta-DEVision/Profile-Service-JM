package com.devision.jm.profile.controller;

import com.devision.jm.profile.api.external.dto.ProfileResponse;
import com.devision.jm.profile.api.external.dto.ProfileUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.ProfileApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileApi profileService;

    /**
     * Get current user's profile
     * GET /api/profile/me
     *
     * Note: userId header is set by API Gateway after validating JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Get profile request for userId: {}", userId);
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile by userId
     * GET /api/profile/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(
            @PathVariable String userId) {
        log.info("Get profile request for userId: {}", userId);
        ProfileResponse response = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get profile by email
     * GET /api/profile/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ProfileResponse> getProfileByEmail(
            @PathVariable String email) {
        log.info("Get profile request for email: {}", email);
        ProfileResponse response = profileService.getProfileByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Update current user's profile
     * PUT /api/profile/me
     */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Update profile request for userId: {}", userId);
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update profile by userId (admin)
     * PUT /api/profile/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Update profile request for userId: {}", userId);
        ProfileResponse response = profileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search profiles (admin)
     * GET /api/profile/search?q={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProfileResponse>> searchProfiles(
            @RequestParam("q") String searchTerm) {
        log.info("Search profiles request with term: {}", searchTerm);
        List<ProfileResponse> response = profileService.searchProfiles(searchTerm);
        return ResponseEntity.ok(response);
    }
}
