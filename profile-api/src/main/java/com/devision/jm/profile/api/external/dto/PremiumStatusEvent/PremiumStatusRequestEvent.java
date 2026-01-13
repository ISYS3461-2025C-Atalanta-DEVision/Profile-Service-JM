package com.devision.jm.profile.api.external.dto.PremiumStatusEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Premium Status Request Event (Kafka DTO)
 *
 * Consumed from Applicant Search Service to query company's premium status.
 *
 * Topic: premium-status.requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumStatusRequestEvent {

    /**
     * Correlation ID for matching request with response
     */
    private String requestId;

    /**
     * Company ID to lookup premium status for
     */
    private String companyId;
}
