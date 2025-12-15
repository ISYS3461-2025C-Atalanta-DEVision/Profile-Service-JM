package com.devision.jm.profile.api.internal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payment Completed Event
 *
 * Consumed from Kafka topic "payment-completed" when a payment succeeds.
 * Published by Payment Service to upgrade user subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {

    /**
     * User ID (from Auth/Profile Service)
     * Used to find the profile to upgrade
     */
    private String userId;

    /**
     * Plan type (PREMIUM)
     */
    private String planType;

    /**
     * Timestamp when payment was completed
     */
    private LocalDateTime paidAt;
}
