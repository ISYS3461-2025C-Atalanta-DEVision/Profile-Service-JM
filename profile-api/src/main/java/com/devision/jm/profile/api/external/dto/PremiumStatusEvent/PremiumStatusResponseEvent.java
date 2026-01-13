package com.devision.jm.profile.api.external.dto.PremiumStatusEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Premium Status Response Event (Kafka DTO)
 *
 * Published to Applicant Search Service with company's premium status.
 *
 * Topic: premium-status.responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumStatusResponseEvent {

    /**
     * Correlation ID matching the request
     */
    private String requestId;

    /**
     * Company ID that was looked up
     */
    private String companyId;

    /**
     * Whether the company is premium
     */
    private Boolean isPremium;
}
