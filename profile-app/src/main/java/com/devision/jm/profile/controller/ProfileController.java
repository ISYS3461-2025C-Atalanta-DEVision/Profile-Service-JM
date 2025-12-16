package com.devision.jm.profile.controller;

import com.devision.jm.profile.api.external.dto.ProfileCreateRequest;
import com.devision.jm.profile.api.external.dto.ProfileFullUpdateRequest;
import com.devision.jm.profile.api.external.dto.ProfileResponse;
import com.devision.jm.profile.api.external.interfaces.ProfileApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
 *
 * Endpoints:
 * - GET  /api/profiles/me           - Get current user's profile
 * - GET  /api/profiles?email=       - Get profile by email (internal use)
 * - GET  /api/profiles?search=      - Search profiles (internal use)
 * - POST /api/profiles              - Create profile (internal use)
 * - PUT  /api/profiles/me           - Update current user's profile
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
     * Get profile by email or search profiles
     * GET /api/profiles?email={email}
     * GET /api/profiles?search={searchTerm}
     */
    @GetMapping
    public ResponseEntity<?> getProfiles(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "search", required = false) String searchTerm) {
        if (email != null) {
            log.info("Get profile request for email: {}", email);
            ProfileResponse response = profileService.getProfileByEmail(email);
            return ResponseEntity.ok(response);
        }
        if (searchTerm != null) {
            log.info("Search profiles request with term: {}", searchTerm);
            List<ProfileResponse> response = profileService.searchProfiles(searchTerm);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body("Either 'email' or 'search' query parameter is required");
    }

    /**
     * Create profile for existing user
     * POST /api/profiles
     */
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(
            @Valid @RequestBody ProfileCreateRequest request) {
        log.info("Create profile request for userId: {}", request.getUserId());
        ProfileResponse response = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
}
