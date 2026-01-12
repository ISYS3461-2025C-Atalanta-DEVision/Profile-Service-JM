package com.devision.jm.profile.service;

import com.devision.jm.profile.api.internal.dto.ShardMigrationEvent;
import com.devision.jm.profile.api.internal.dto.ShardMigrationEvent.MigrationStatus;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Shard Migration Service
 *
 * Handles data migration when a company changes their country (shard key).
 *
 * Requirement 3.3.1 (Ultimo):
 * - Any profile modification must be persisted immediately to the appropriate database shard.
 * - If the company changes the Country field, the application logic must perform
 *   a data migration of the entire user record to the new, corresponding database shard.
 *
 * Architecture (A.3.4):
 * - Database sharding techniques to partition and query different shards.
 *
 * Note: In a real sharded MongoDB setup, this would involve:
 * 1. Reading the document from the source shard
 * 2. Deleting from source shard
 * 3. Inserting to destination shard (based on new country shard key)
 *
 * For this implementation:
 * - We simulate shard migration by updating the document with the new country
 * - We publish a Kafka event for audit/notification purposes
 * - In a production sharded cluster, MongoDB would automatically route the document
 *   to the correct shard based on the shard key (country)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShardMigrationService {

    private final ProfileRepository profileRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.shard-migration:profile.shard.migration}")
    private String shardMigrationTopic;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    /**
     * Migrate profile data when country (shard key) changes
     *
     * This method:
     * 1. Publishes INITIATED event
     * 2. Performs the migration (update with new country)
     * 3. Publishes COMPLETED or FAILED event
     *
     * @param profile The profile being updated
     * @param previousCountry The old country (source shard)
     * @param newCountry The new country (destination shard)
     * @return The migrated profile
     */
    @Transactional
    public Profile migrateProfileToNewShard(Profile profile, String previousCountry, String newCountry) {
        log.info("Starting shard migration for profile. userId={}, profileId={}, from={} to={}",
                profile.getUserId(), profile.getId(), previousCountry, newCountry);

        LocalDateTime initiatedAt = LocalDateTime.now();

        // Publish INITIATED event
        publishMigrationEvent(profile, previousCountry, newCountry, MigrationStatus.INITIATED, null, initiatedAt, null);

        try {
            // ==================== SHARD MIGRATION LOGIC ====================
            //
            // In a real MongoDB sharded cluster:
            // - The shard key would be 'country'
            // - MongoDB would route documents to shards based on country
            // - Changing the country field would trigger MongoDB's chunk migration
            //
            // For this implementation, we:
            // 1. Create a complete copy of the profile data
            // 2. Update the country field (shard key)
            // 3. Save to the repository (MongoDB routes to correct shard)
            // 4. The old document is effectively replaced
            //
            // Note: In MongoDB 4.2+, shard key values CAN be updated if:
            // - The transaction is used
            // - The update uses the full shard key

            // Update the country (triggers MongoDB to route to new shard)
            profile.setCountry(newCountry);

            // Save the profile - MongoDB handles shard routing
            Profile migratedProfile = profileRepository.save(profile);

            LocalDateTime completedAt = LocalDateTime.now();

            // Publish COMPLETED event
            publishMigrationEvent(migratedProfile, previousCountry, newCountry,
                    MigrationStatus.COMPLETED, null, initiatedAt, completedAt);

            log.info("Shard migration completed successfully. userId={}, profileId={}, from={} to={}, duration={}ms",
                    profile.getUserId(), profile.getId(), previousCountry, newCountry,
                    java.time.Duration.between(initiatedAt, completedAt).toMillis());

            return migratedProfile;

        } catch (Exception e) {
            log.error("Shard migration failed. userId={}, profileId={}, from={} to={}, error={}",
                    profile.getUserId(), profile.getId(), previousCountry, newCountry, e.getMessage(), e);

            // Publish FAILED event
            publishMigrationEvent(profile, previousCountry, newCountry,
                    MigrationStatus.FAILED, e.getMessage(), initiatedAt, LocalDateTime.now());

            throw new RuntimeException("Shard migration failed for profile " + profile.getId(), e);
        }
    }

    /**
     * Check if the country change requires shard migration
     *
     * @param previousCountry The old country
     * @param newCountry The new country
     * @return true if migration is needed
     */
    public boolean requiresMigration(String previousCountry, String newCountry) {
        if (previousCountry == null && newCountry == null) {
            return false;
        }
        if (previousCountry == null || newCountry == null) {
            return true; // Going from null to value or value to null
        }
        return !previousCountry.equalsIgnoreCase(newCountry);
    }

    /**
     * Publish shard migration event to Kafka
     */
    private void publishMigrationEvent(Profile profile, String previousCountry, String newCountry,
                                        MigrationStatus status, String errorMessage,
                                        LocalDateTime initiatedAt, LocalDateTime completedAt) {
        if (!kafkaEnabled) {
            log.debug("Kafka disabled, skipping shard migration event. status={}", status);
            return;
        }

        try {
            ShardMigrationEvent event = ShardMigrationEvent.builder()
                    .eventType("SHARD_MIGRATION")
                    .userId(profile.getUserId())
                    .profileId(profile.getId())
                    .email(profile.getEmail())
                    .companyName(profile.getCompanyName())
                    .previousCountry(previousCountry)
                    .newCountry(newCountry)
                    .status(status)
                    .errorMessage(errorMessage)
                    .initiatedAt(initiatedAt)
                    .completedAt(completedAt)
                    .build();

            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(shardMigrationTopic, profile.getUserId(), json);

            log.info("Published shard migration event. userId={}, status={}, topic={}",
                    profile.getUserId(), status, shardMigrationTopic);

        } catch (Exception e) {
            // Don't fail the migration if event publishing fails
            log.error("Failed to publish shard migration event. userId={}, status={}, error={}",
                    profile.getUserId(), status, e.getMessage(), e);
        }
    }

    /**
     * Get the shard identifier for a given country
     *
     * This method returns which shard a country maps to.
     * In a real implementation, this would match MongoDB's shard key ranges.
     *
     * Example shard strategy:
     * - APAC: Vietnam, Singapore, Australia, Japan, China, etc.
     * - EMEA: Germany, UK, France, etc.
     * - AMERICAS: USA, Canada, Brazil, etc.
     *
     * @param country The country name
     * @return The shard identifier
     */
    public String getShardForCountry(String country) {
        if (country == null) {
            return "DEFAULT";
        }

        // Normalize country name
        String normalizedCountry = country.toUpperCase().trim();

        // APAC region
        if (normalizedCountry.contains("VIETNAM") ||
            normalizedCountry.contains("SINGAPORE") ||
            normalizedCountry.contains("AUSTRALIA") ||
            normalizedCountry.contains("JAPAN") ||
            normalizedCountry.contains("CHINA") ||
            normalizedCountry.contains("KOREA") ||
            normalizedCountry.contains("INDIA") ||
            normalizedCountry.contains("MALAYSIA") ||
            normalizedCountry.contains("THAILAND") ||
            normalizedCountry.contains("INDONESIA") ||
            normalizedCountry.contains("PHILIPPINES")) {
            return "SHARD_APAC";
        }

        // EMEA region
        if (normalizedCountry.contains("GERMANY") ||
            normalizedCountry.contains("UNITED KINGDOM") ||
            normalizedCountry.contains("UK") ||
            normalizedCountry.contains("FRANCE") ||
            normalizedCountry.contains("ITALY") ||
            normalizedCountry.contains("SPAIN") ||
            normalizedCountry.contains("NETHERLANDS") ||
            normalizedCountry.contains("SWEDEN") ||
            normalizedCountry.contains("NORWAY") ||
            normalizedCountry.contains("DENMARK") ||
            normalizedCountry.contains("SWITZERLAND")) {
            return "SHARD_EMEA";
        }

        // Americas region
        if (normalizedCountry.contains("UNITED STATES") ||
            normalizedCountry.contains("USA") ||
            normalizedCountry.contains("CANADA") ||
            normalizedCountry.contains("BRAZIL") ||
            normalizedCountry.contains("MEXICO") ||
            normalizedCountry.contains("ARGENTINA")) {
            return "SHARD_AMERICAS";
        }

        // Default shard for other countries
        return "SHARD_DEFAULT";
    }
}
