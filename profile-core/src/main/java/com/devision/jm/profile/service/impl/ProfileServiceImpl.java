package com.devision.jm.profile.service.impl;

import com.devision.jm.profile.api.external.dto.ApplicantSearchProfileDto;
import com.devision.jm.profile.api.external.dto.MediaItemDto;
import com.devision.jm.profile.api.external.dto.ProfileResponse;
import com.devision.jm.profile.api.external.dto.ProfileUpdateRequest;
import com.devision.jm.profile.api.external.interfaces.ProfileApi;
import com.devision.jm.profile.model.embedded.ApplicantSearchProfile;
import com.devision.jm.profile.model.embedded.MediaItem;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.model.enums.EducationDegree;
import com.devision.jm.profile.model.enums.EmploymentStatus;
import com.devision.jm.profile.model.enums.MediaType;
import com.devision.jm.profile.model.enums.SubscriptionType;
import com.devision.jm.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
 * - 3.2.2: Media Gallery
 * - 6.2.1-6.2.4: Applicant Search Profile (Premium Feature)
 */
@Slf4j
@Service
@RequiredArgsConstructor
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
    @Transactional
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

        // ==================== Media Gallery (3.2.2) ====================
        if (request.getMediaGallery() != null) {
            List<MediaItem> mediaItems = request.getMediaGallery().stream()
                    .map(this::toMediaItem)
                    .collect(Collectors.toList());
            profile.setMediaGallery(mediaItems);
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
                // Media Gallery
                .mediaGallery(toMediaItemDtos(profile.getMediaGallery()))
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
     * Convert MediaItem entity to MediaItemDto
     */
    private MediaItemDto toMediaItemDto(MediaItem item) {
        if (item == null) {
            return null;
        }
        return MediaItemDto.builder()
                .mediaType(item.getMediaType() != null ? item.getMediaType().name() : null)
                .url(item.getUrl())
                .thumbnailUrl(item.getThumbnailUrl())
                .title(item.getTitle())
                .description(item.getDescription())
                .displayOrder(item.getDisplayOrder())
                .uploadedAt(item.getUploadedAt())
                .build();
    }

    /**
     * Convert list of MediaItem entities to DTOs
     */
    private List<MediaItemDto> toMediaItemDtos(List<MediaItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(this::toMediaItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert MediaItemDto to MediaItem entity
     */
    private MediaItem toMediaItem(MediaItemDto dto) {
        if (dto == null) {
            return null;
        }
        return MediaItem.builder()
                .mediaType(dto.getMediaType() != null ? MediaType.valueOf(dto.getMediaType()) : null)
                .url(dto.getUrl())
                .thumbnailUrl(dto.getThumbnailUrl())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .displayOrder(dto.getDisplayOrder())
                .uploadedAt(dto.getUploadedAt() != null ? dto.getUploadedAt() : LocalDateTime.now())
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
