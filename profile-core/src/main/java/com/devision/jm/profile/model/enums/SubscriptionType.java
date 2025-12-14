package com.devision.jm.profile.model.enums;

/**
 * Subscription Type Enum
 *
 * Represents the subscription tier for a company.
 * Updated via Kafka events from Payment Service.
 *
 * Implements requirements:
 * - 6.1.1: Subscription tiers (Free trial, Monthly, Yearly)
 */
public enum SubscriptionType {
    FREE_TRIAL,    // 14-day free trial
    MONTHLY,       // Monthly subscription
    YEARLY,        // Yearly subscription (discounted)
    EXPIRED        // Subscription has expired
}
