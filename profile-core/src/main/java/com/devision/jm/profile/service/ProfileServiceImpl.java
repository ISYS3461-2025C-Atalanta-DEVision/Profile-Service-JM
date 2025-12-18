package com.devision.jm.profile.service;

import com.devision.jm.profile.api.external.dto.ApplicantSearchProfileDto;
import com.devision.jm.profile.api.external.dto.ProfileCreateRequest;
import com.devision.jm.profile.api.external.dto.ProfileFullUpdateRequest;
import com.devision.jm.profile.api.external.dto.ProfileResponse;
import com.devision.jm.profile.api.external.dto.ProfileUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.ProfileApi;
import com.devision.jm.profile.model.embedded.ApplicantSearchProfile;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.model.enums.EducationDegree;
import com.devision.jm.profile.model.enums.EmploymentStatus;
import com.devision.jm.profile.model.enums.SubscriptionType;
import com.devision.jm.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Profile Service Implementation
 *
 * Main service implementing external ProfileApi interface.
 * This is the Business Logic Layer (A.2.2) for profile management.
 *
 * Microservice Architecture (A.3.1):
 * - Profile Service owns: company info, contact info, subscription
 * - Auth Service owns: email, password, tokens
 *
 * Implements requirements:
 * - 3.1.2: About Us, Who We Are Looking For
 * - 3.2.1: Company Logo
 * - 6.2.1-6.2.4: Applicant Search Profile (Premium Feature)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ProfileServiceImpl implements ProfileApi {

    private final ProfileRepository profileRepository;

    @Override
    public ProfileResponse getProfileByUserId(String userId) {
        log.info("Getting profile for userId: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        return toProfileResponse(profile);
    }

    @Override
    public ProfileResponse getProfileByEmail(String email) {
        log.info("Getting profile for email: {}", email);

        Profile profile = profileRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Profile not found for email: " + email));

        return toProfileResponse(profile);
    }

    @Override
    public ProfileResponse updateProfile(String userId, ProfileUpdateRequest request) {
        log.info("Updating profile for userId: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        // ==================== Basic Company Info ====================
        if (request.getCompanyName() != null) {
            profile.setCompanyName(request.getCompanyName());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getLogoUrl() != null) {
            profile.setLogoUrl(request.getLogoUrl());
        }
        if (request.getAboutUs() != null) {
            profile.setAboutUs(request.getAboutUs());
        }
        if (request.getWhoWeAreLookingFor() != null) {
            profile.setWhoWeAreLookingFor(request.getWhoWeAreLookingFor());
        }

        // ==================== Location Info ====================
        if (request.getCountry() != null) {
            profile.setCountry(request.getCountry());
        }
        if (request.getCity() != null) {
            profile.setCity(request.getCity());
        }
        if (request.getStreetAddress() != null) {
            profile.setStreetAddress(request.getStreetAddress());
        }

        // ==================== Contact Info ====================
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }

        // ==================== Applicant Search Profile (6.2.1-6.2.4) ====================
        if (request.getApplicantSearchProfile() != null) {
            // Check if this is a premium feature
            if (!profile.isPremiumSubscriber()) {
                log.warn("User {} attempted to update applicant search profile without premium subscription", userId);
                throw new RuntimeException("Applicant Search Profile is a premium feature. Please upgrade your subscription.");
            }
            ApplicantSearchProfile searchProfile = toApplicantSearchProfile(request.getApplicantSearchProfile());
            profile.setApplicantSearchProfile(searchProfile);
        }

        Profile updatedProfile = profileRepository.save(profile);
        log.info("Profile updated successfully for userId: {}", userId);

        return toProfileResponse(updatedProfile);
    }

    @Override
    public List<ProfileResponse> searchProfiles(String searchTerm) {
        log.info("Searching profiles with term: {}", searchTerm);

        List<Profile> profiles = profileRepository
                .findByEmailContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(searchTerm, searchTerm);

        return profiles.stream()
                .map(this::toProfileResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProfileResponse createProfile(ProfileCreateRequest request) {
        log.info("Creating profile for userId: {}, email: {}", request.getUserId(), request.getEmail());

        // Check if profile already exists
        if (profileRepository.existsByUserId(request.getUserId())) {
            log.warn("Profile already exists for userId: {}", request.getUserId());
            throw new RuntimeException("Profile already exists for userId: " + request.getUserId());
        }

        // Create new profile with FREE subscription
        Profile profile = Profile.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .companyName(request.getCompanyName())
                .country(request.getCountry())
                .city(request.getCity())
                .streetAddress(request.getStreetAddress())
                .phoneNumber(request.getPhoneNumber())
                .authProvider(request.getAuthProvider())
                .subscriptionType(SubscriptionType.FREE)
                // avatarUrl will use default value from Profile entity
                .build();

        Profile savedProfile = profileRepository.save(profile);
        log.info("Profile created successfully for userId: {}, profileId: {}",
                request.getUserId(), savedProfile.getId());

        return toProfileResponse(savedProfile);
    }

    @Override
    public ProfileResponse fullUpdateProfile(String userId, ProfileFullUpdateRequest request) {
        log.info("Full update profile for userId: {}", userId);

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for userId: " + userId));

        // ==================== Email Update ====================
        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equalsIgnoreCase(profile.getEmail())) {
            // Check if new email is already in use by another profile
            if (profileRepository.existsByEmailIgnoreCase(newEmail)) {
                log.warn("Email {} is already in use", newEmail);
                throw new RuntimeException("Email is already in use: " + newEmail);
            }
            log.info("Updating email for userId: {} from {} to {}", userId, profile.getEmail(), newEmail);
            profile.setEmail(newEmail);
        }

        // ==================== Contact Info ====================
        profile.setCompanyName(request.getCompanyName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setCountry(request.getCountry());
        profile.setCity(request.getCity());
        profile.setStreetAddress(request.getStreetAddress());

        // ==================== Public Profile (3.1.2) ====================
        profile.setAboutUs(request.getAboutUs());
        profile.setWhoWeAreLookingFor(request.getWhoWeAreLookingFor());

        Profile updatedProfile = profileRepository.save(profile);
        log.info("Profile fully updated for userId: {}", userId);

        return toProfileResponse(updatedProfile);
    }

    // ==================== Conversion Methods ====================

    /**
     * Convert Profile entity to ProfileResponse DTO
     */
    private ProfileResponse toProfileResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .email(profile.getEmail())
                // Basic Company Info
                .companyName(profile.getCompanyName())
                .avatarUrl(profile.getAvatarUrl())
                .logoUrl(profile.getLogoUrl())
                .aboutUs(profile.getAboutUs())
                .whoWeAreLookingFor(profile.getWhoWeAreLookingFor())
                // Location Info
                .country(profile.getCountry())
                .city(profile.getCity())
                .streetAddress(profile.getStreetAddress())
                // Contact Info
                .phoneNumber(profile.getPhoneNumber())
                // Subscription Info
                .subscriptionType(profile.getSubscriptionType() != null ?
                        profile.getSubscriptionType().name() : null)
                .subscriptionStartDate(profile.getSubscriptionStartDate())
                .subscriptionEndDate(profile.getSubscriptionEndDate())
                .daysRemaining(profile.getDaysRemaining())
                .subscriptionActive(profile.isSubscriptionActive())
                // Applicant Search Profile
                .applicantSearchProfile(toApplicantSearchProfileDto(profile.getApplicantSearchProfile()))
                // Metadata
                .authProvider(profile.getAuthProvider())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    /**
     * Convert ApplicantSearchProfile entity to DTO
     */
    private ApplicantSearchProfileDto toApplicantSearchProfileDto(ApplicantSearchProfile profile) {
        if (profile == null) {
            return null;
        }
        return ApplicantSearchProfileDto.builder()
                .desiredTechnicalSkills(profile.getDesiredTechnicalSkills())
                .desiredEmploymentStatus(profile.getDesiredEmploymentStatus() != null ?
                        profile.getDesiredEmploymentStatus().stream()
                                .map(EmploymentStatus::name)
                                .collect(Collectors.toList()) : null)
                .desiredCountry(profile.getDesiredCountry())
                .desiredSalaryMin(profile.getDesiredSalaryMin())
                .desiredSalaryMax(profile.getDesiredSalaryMax())
                .salaryCurrency(profile.getSalaryCurrency())
                .desiredEducationDegree(profile.getDesiredEducationDegree() != null ?
                        profile.getDesiredEducationDegree().name() : null)
                .build();
    }

    /**
     * Convert ApplicantSearchProfileDto to entity
     */
    private ApplicantSearchProfile toApplicantSearchProfile(ApplicantSearchProfileDto dto) {
        if (dto == null) {
            return null;
        }
        return ApplicantSearchProfile.builder()
                .desiredTechnicalSkills(dto.getDesiredTechnicalSkills())
                .desiredEmploymentStatus(dto.getDesiredEmploymentStatus() != null ?
                        dto.getDesiredEmploymentStatus().stream()
                                .map(EmploymentStatus::valueOf)
                                .collect(Collectors.toList()) : null)
                .desiredCountry(dto.getDesiredCountry())
                .desiredSalaryMin(dto.getDesiredSalaryMin())
                .desiredSalaryMax(dto.getDesiredSalaryMax())
                .salaryCurrency(dto.getSalaryCurrency() != null ? dto.getSalaryCurrency() : "USD")
                .desiredEducationDegree(dto.getDesiredEducationDegree() != null ?
                        EducationDegree.valueOf(dto.getDesiredEducationDegree()) : null)
                .build();
    }
}
