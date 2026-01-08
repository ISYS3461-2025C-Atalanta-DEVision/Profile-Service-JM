package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Subscription Changed Event
 *
 * Published to Kafka when a company's subscription status changes.
 * Consumed by Applicant-Search-Service to update isPremium flag.
 *
 * Topic: subscription.changed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionChangedEvent {

    /**
     * Company ID (userId)
     */
    private String companyId;

    /**
     * Whether the company is now premium
     */
    private Boolean isPremium;

    /**
     * Subscription type: FREE or PREMIUM
     */
    private String subscriptionType;

    /**
     * When the change occurred
     */
    private LocalDateTime changedAt;
}
