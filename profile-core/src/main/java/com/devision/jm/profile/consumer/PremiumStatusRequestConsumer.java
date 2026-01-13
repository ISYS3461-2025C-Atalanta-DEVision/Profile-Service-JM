package com.devision.jm.profile.consumer;

import com.devision.jm.profile.api.external.dto.PremiumStatusEvent.PremiumStatusRequestEvent;
import com.devision.jm.profile.api.external.dto.PremiumStatusEvent.PremiumStatusResponseEvent;
import com.devision.jm.profile.model.enums.SubscriptionType;
import com.devision.jm.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Premium Status Request Consumer
 *
 * Handles premium status requests from Applicant Search Service.
 * Returns the company's current premium status via Kafka response.
 *
 * Topic: premium-status.requests
 * Response Topic: premium-status.responses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumStatusRequestConsumer {

    private final ProfileRepository profileRepository;
    private final KafkaTemplate<String, PremiumStatusResponseEvent> premiumStatusResponseKafkaTemplate;

    @KafkaListener(
            topics = "premium-status.requests",
            groupId = "profile-service",
            containerFactory = "premiumStatusRequestListenerContainerFactory"
    )
    public void handlePremiumStatusRequest(PremiumStatusRequestEvent event) {
        String companyId = event.getCompanyId();
        log.info("Received premium-status.request for companyId={} requestId={}",
                companyId, event.getRequestId());

        Boolean isPremium = profileRepository.findByUserId(companyId)
                .map(profile -> profile.getSubscriptionType() == SubscriptionType.PREMIUM
                        && profile.isSubscriptionActive())
                .orElse(false);

        PremiumStatusResponseEvent response = PremiumStatusResponseEvent.builder()
                .requestId(event.getRequestId())
                .companyId(companyId)
                .isPremium(isPremium)
                .build();

        premiumStatusResponseKafkaTemplate.send("premium-status.responses", companyId, response);
        log.info("Sent premium-status.response for companyId={} isPremium={}", companyId, isPremium);
    }
}
