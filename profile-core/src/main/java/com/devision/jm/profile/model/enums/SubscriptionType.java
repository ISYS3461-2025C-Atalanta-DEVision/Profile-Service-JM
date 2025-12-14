package com.devision.jm.profile.model.enums;

/**
 * Subscription Type Enum
 *
 * Represents the subscription tier for a company.
 * Updated via Kafka events from Payment Service.
 *
 * Implements requirements:
 * - 6.1.1: Free and Premium subscription tiers
 */
public enum SubscriptionType {
    FREE,      // Free tier (no subscription)
    PREMIUM    // Premium subscription ($30/month)
}
