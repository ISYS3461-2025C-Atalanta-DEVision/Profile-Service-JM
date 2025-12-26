package com.devision.jm.profile.kafka;

import com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameRequestEvent;
import com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameResponseEvent;
import com.devision.jm.profile.model.entity.Profile;
import com.devision.jm.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyNameRequestListener {

    private final ProfileRepository profileRepository;
    private final KafkaTemplate<String, CompanyNameResponseEvent> companyNameResponseKafkaTemplate;

    @KafkaListener(
            topics = "company-name.requests",
            groupId = "profile-service",
            containerFactory = "companyNameRequestListenerContainerFactory"
    )
    public void handleCompanyNameRequest(CompanyNameRequestEvent event) {
        String companyId = event.getCompanyId();
        log.info("Received company-name.request for companyId={} requestId={}",
                 companyId, event.getRequestId());

        String companyName = profileRepository.findById(companyId)
                .map(Profile::getCompanyName)
                .orElse(null);

        CompanyNameResponseEvent response = CompanyNameResponseEvent.builder()
                .requestId(event.getRequestId())
                .companyId(companyId)
                .companyName(companyName)
                .build();

        companyNameResponseKafkaTemplate.send("company-name.responses", companyId, response);
        log.info("Sent company-name.response for companyId={} name={}", companyId, companyName);
    }
}