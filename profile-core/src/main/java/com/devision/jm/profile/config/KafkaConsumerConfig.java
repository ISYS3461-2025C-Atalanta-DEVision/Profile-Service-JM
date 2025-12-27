package com.devision.jm.profile.config;

import com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    private final KafkaDiscoveryService kafkaDiscoveryService;

    @Bean
    public ConsumerFactory<String, CompanyNameRequestEvent> companyNameRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring CompanyNameRequest Kafka Consumer with bootstrap servers: {}", bootstrapServers);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);

        // Option A (recommended for now): Trust all
        props.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Optionally keep VALUE_DEFAULT_TYPE, but it is not required if we trust all
        // and the producer sends __TypeId__ header.
        // You can either remove it, or set it to your local class:
        // props.put(org.springframework.kafka.support.serializer.JsonDeserializer.VALUE_DEFAULT_TYPE,
        // "com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameRequestEvent");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "profile-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "companyNameRequestListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CompanyNameRequestEvent> companyNameRequestListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, CompanyNameRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(companyNameRequestConsumerFactory());
        return factory;
    }
}