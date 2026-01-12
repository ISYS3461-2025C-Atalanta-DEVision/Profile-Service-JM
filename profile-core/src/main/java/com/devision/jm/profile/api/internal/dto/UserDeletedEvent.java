package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Deleted Event (Internal DTO)
 *
 * Kafka event consumed when a user/company is deleted by admin from Auth Service.
 * Profile Service uses this to clean up profile and related data.
 *
 * Microservice Architecture (A.3.2):
 * - Communication among microservices via Message Broker (Kafka)
 *
 * Topic: user-deleted
 * Producer: Auth Service
 *
 * Cleanup Actions:
 * - Delete profile document
 * - Delete all company events
 * - (Future) Notify File Service to delete S3 files
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {

    /**
     * User ID from Auth Service (MongoDB ObjectId as String)
     * This is the reference key used to find and delete profile
     */
    private String userId;

    /**
     * User's email - for audit/logging purposes
     */
    private String email;

    /**
     * Reason for deletion (optional)
     */
    private String reason;

    /**
     * Admin who performed the deletion (optional)
     */
    private String deletedBy;

    /**
     * Event timestamp
     */
    private LocalDateTime deletedAt;
}
