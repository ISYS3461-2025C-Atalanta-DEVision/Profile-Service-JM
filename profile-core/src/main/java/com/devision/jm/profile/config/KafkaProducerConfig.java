package com.devision.jm.profile.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import com.devision.jm.profile.model.entity.Profile;

import com.devision.jm.profile.api.external.dto.ProfileUpdateEventResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration
 *
 * Configures Kafka producer for sending events to File Service.
 * Uses Eureka discovery to find Kafka broker address.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaProducerConfig {

    private final KafkaDiscoveryService kafkaDiscoveryService;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring Kafka Producer with bootstrap servers: {}", bootstrapServers);

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Autowired
    private KafkaTemplate<String, ProfileUpdateEventResponse> kafkaTemplate;

    public void handleCompanyUpdated(Profile company) {
        ProfileUpdateEventResponse event = ProfileUpdateEventResponse.builder()
                .userId(company.getId()) // companyId in other services
                .companyName(company.getCompanyName())
                .avatarUrl(company.getAvatarUrl())
                .logoUrl(company.getLogoUrl())
                .country(company.getCountry())
                .city(company.getCity())
                .streetAddress(company.getStreetAddress())
                .phoneNumber(company.getPhoneNumber())
                .build();

        // Publish to topic consumed by Job Post and other services
        kafkaTemplate.send("company-profile.updates", company.getId(), event);
    }
}
