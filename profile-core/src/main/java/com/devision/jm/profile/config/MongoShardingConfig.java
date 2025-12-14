package com.devision.jm.profile.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

/**
 * MongoDB Sharding Configuration
 *
 * Implements requirements:
 * - 1.3.3: Database sharding by country
 * - 3.3.1: Sharding support for profile data
 *
 * MongoDB Sharding Setup (Infrastructure Level):
 *
 * To enable sharding for the profiles collection, run these commands
 * on the MongoDB admin database:
 *
 * 1. Enable sharding on the database:
 *    sh.enableSharding("profile_db")
 *
 * 2. Shard the profiles collection by country:
 *    sh.shardCollection("profile_db.profiles", { "country": 1 })
 *
 * Alternatively, use a compound shard key for better distribution:
 *    sh.shardCollection("profile_db.profiles", { "country": 1, "_id": 1 })
 *
 * The country field is indexed and used as the shard key because:
 * - Most queries filter by country (geographic locality)
 * - Data is naturally distributed by country
 * - Supports data residency requirements
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoShardingConfig {

    private final MongoTemplate mongoTemplate;

    /**
     * Create indexes optimized for sharding on application startup.
     * The country index is essential for sharding to work efficiently.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createShardingIndexes() {
        log.info("Creating MongoDB indexes for sharding support (3.3.1)...");

        try {
            // Primary shard key index: country
            // This index is required for MongoDB to use country as the shard key
            mongoTemplate.indexOps("profiles").ensureIndex(
                    new Index()
                            .on("country", Sort.Direction.ASC)
                            .named("idx_country_shard")
            );
            log.info("Created index: idx_country_shard");

            // Compound index for common queries with country
            mongoTemplate.indexOps("profiles").ensureIndex(
                    new Index()
                            .on("country", Sort.Direction.ASC)
                            .on("subscription_type", Sort.Direction.ASC)
                            .named("idx_country_subscription")
            );
            log.info("Created index: idx_country_subscription");

            // Compound index for country + company name searches
            mongoTemplate.indexOps("profiles").ensureIndex(
                    new Index()
                            .on("country", Sort.Direction.ASC)
                            .on("company_name", Sort.Direction.ASC)
                            .named("idx_country_company")
            );
            log.info("Created index: idx_country_company");

            // Index for subscription expiry queries (6.1.2)
            mongoTemplate.indexOps("profiles").ensureIndex(
                    new Index()
                            .on("subscription_end_date", Sort.Direction.ASC)
                            .on("subscription_type", Sort.Direction.ASC)
                            .named("idx_subscription_expiry")
            );
            log.info("Created index: idx_subscription_expiry");

            log.info("MongoDB sharding indexes created successfully");

        } catch (Exception e) {
            log.warn("Failed to create MongoDB indexes (may already exist): {}", e.getMessage());
        }
    }
}
