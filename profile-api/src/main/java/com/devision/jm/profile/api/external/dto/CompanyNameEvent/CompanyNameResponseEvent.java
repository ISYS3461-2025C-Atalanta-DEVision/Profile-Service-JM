package com.devision.jm.profile.api.external.dto.CompanyNameEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyNameResponseEvent {
    private String requestId;
    private String companyId;
    private String companyName;
}
