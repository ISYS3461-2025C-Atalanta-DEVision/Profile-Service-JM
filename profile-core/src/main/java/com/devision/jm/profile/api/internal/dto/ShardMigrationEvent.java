package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shard Migration Event (Internal DTO)
 *
 * Published when a user changes their country, triggering a data migration
 * to a new database shard.
 *
 * Requirement 3.3.1 (Ultimo):
 * - Any profile modification must be persisted immediately to the appropriate database shard.
 * - If the company changes the Country field, the application logic must perform
 *   a data migration of the entire user record to the new, corresponding database shard.
 *
 * Microservice Architecture (A.3.2):
 * - Communication among microservices via Message Broker (Kafka)
 *
 * Topic: profile.shard.migration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShardMigrationEvent {

    /**
     * Event type identifier
     */
    @Builder.Default
    private String eventType = "SHARD_MIGRATION";

    /**
     * User ID from Auth Service
     */
    private String userId;

    /**
     * Profile ID (MongoDB _id)
     */
    private String profileId;

    /**
     * Email for audit purposes
     */
    private String email;

    /**
     * Company name for audit purposes
     */
    private String companyName;

    /**
     * Previous country (source shard)
     */
    private String previousCountry;

    /**
     * New country (destination shard)
     */
    private String newCountry;

    /**
     * Migration status
     */
    private MigrationStatus status;

    /**
     * Error message if migration failed
     */
    private String errorMessage;

    /**
     * Timestamp when migration was initiated
     */
    private LocalDateTime initiatedAt;

    /**
     * Timestamp when migration was completed
     */
    private LocalDateTime completedAt;

    /**
     * Migration status enum
     */
    public enum MigrationStatus {
        INITIATED,      // Migration started
        IN_PROGRESS,    // Data being migrated
        COMPLETED,      // Successfully migrated
        FAILED,         // Migration failed
        ROLLED_BACK     // Migration rolled back
    }
}
