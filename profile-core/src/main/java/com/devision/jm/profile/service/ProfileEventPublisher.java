package com.devision.jm.profile.service;

import com.devision.jm.profile.api.external.dto.ProfileUpdateEventResponse;
import com.devision.jm.profile.model.entity.Profile;    
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileEventPublisher {

    private final KafkaTemplate<String, ProfileUpdateEventResponse> profileUpdateKafkaTemplate;

    public void handleCompanyUpdated(Profile company) {
        ProfileUpdateEventResponse event = ProfileUpdateEventResponse.builder()
                .userId(company.getId())
                .companyName(company.getCompanyName())
                .avatarUrl(company.getAvatarUrl())
                .logoUrl(company.getLogoUrl())
                .country(company.getCountry())
                .city(company.getCity())
                .streetAddress(company.getStreetAddress())
                .phoneNumber(company.getPhoneNumber())
                .build();

        profileUpdateKafkaTemplate.send("company-profile.updates", company.getId(), event);
        log.info("Sent company-profile.update event for companyId={} name={}",
                company.getId(), company.getCompanyName());
    }
}