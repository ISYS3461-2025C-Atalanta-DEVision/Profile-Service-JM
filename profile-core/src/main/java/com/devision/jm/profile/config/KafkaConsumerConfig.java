package com.devision.jm.profile.config;

import com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameRequestEvent;
import com.devision.jm.profile.api.external.dto.PremiumStatusEvent.PremiumStatusRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerConfig {

    private final KafkaDiscoveryService kafkaDiscoveryService;

    // SASL/SSL authentication for Confluent Cloud
    @Value("${KAFKA_SECURITY_PROTOCOL:#{null}}")
    private String securityProtocol;

    @Value("${KAFKA_SASL_MECHANISM:#{null}}")
    private String saslMechanism;

    @Value("${KAFKA_SASL_USERNAME:#{null}}")
    private String saslUsername;

    @Value("${KAFKA_SASL_PASSWORD:#{null}}")
    private String saslPassword;

    private void addSaslConfig(Map<String, Object> configProps) {
        if (securityProtocol != null && saslUsername != null && saslPassword != null) {
            log.info("Configuring SASL/SSL authentication for Kafka Consumer");
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            configProps.put(SaslConfigs.SASL_MECHANISM, saslMechanism != null ? saslMechanism : "PLAIN");
            String jaasConfig = String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword);
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }
    }

    @Bean
    public ConsumerFactory<String, CompanyNameRequestEvent> companyNameRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring CompanyNameRequest Kafka Consumer with bootstrap servers: {}", bootstrapServers);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Tell JsonDeserializer to use our local class, and ignore type headers
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameRequestEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Trusted packages can be just your own package now
        props.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.devision.jm.profile.api.external.dto.CompanyNameEvent");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "profile-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        addSaslConfig(props);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "companyNameRequestListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, CompanyNameRequestEvent> companyNameRequestListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, CompanyNameRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(companyNameRequestConsumerFactory());
        return factory;
    }

    // === Premium Status Request Consumer ===

    @Bean
    public ConsumerFactory<String, PremiumStatusRequestEvent> premiumStatusRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring PremiumStatusRequest Kafka Consumer with bootstrap servers: {}", bootstrapServers);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.devision.jm.profile.api.external.dto.PremiumStatusEvent.PremiumStatusRequestEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        props.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.devision.jm.profile.api.external.dto.PremiumStatusEvent");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "profile-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        addSaslConfig(props);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "premiumStatusRequestListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PremiumStatusRequestEvent> premiumStatusRequestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PremiumStatusRequestEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(premiumStatusRequestConsumerFactory());
        return factory;
    }
}