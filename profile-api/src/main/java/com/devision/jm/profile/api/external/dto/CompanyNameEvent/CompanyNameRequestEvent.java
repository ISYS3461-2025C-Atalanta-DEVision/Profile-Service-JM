package com.devision.jm.profile.api.external.dto.CompanyNameEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Company Name Event DTOs (External)
 *
 * These DTOs are used for requesting and responding with company name information.
 * They facilitate inter-service communication where only the company name is needed.
 *
 * Purpose:
 * - CompanyNameRequestEvent: Used to request the company name by companyId.
 * - CompanyNameResponseEvent: Used to respond with the requested company name.
 *
 * Fields:
 * - requestId:   Unique identifier for tracking the request/response pair.
 * - companyId:   Identifier of the company whose name is being requested/responded.
 * - companyName: The public display name of the company (in response event).
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyNameRequestEvent {
    private String requestId;
    private String companyId;
    private String companyName;
}

