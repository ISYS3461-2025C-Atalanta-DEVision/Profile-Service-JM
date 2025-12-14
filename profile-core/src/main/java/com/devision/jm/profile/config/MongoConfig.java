package com.devision.jm.profile.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Configuration
 *
 * Enables MongoDB auditing for automatic createdAt/updatedAt timestamps.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB auditing enabled via annotation
}
