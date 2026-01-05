package com.devision.jm.profile.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.devision.jm.profile.api.external.dto.ProfileUpdateEventResponse;
import com.devision.jm.profile.api.external.dto.CompanyNameEvent.CompanyNameResponseEvent;

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
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
@Slf4j
public class KafkaProducerConfig {

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
            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            configProps.put(SaslConfigs.SASL_MECHANISM, saslMechanism != null ? saslMechanism : "PLAIN");
            String jaasConfig = String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword);
            configProps.put(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }
    }

    // 1) Basic String → String template (for existing EventServiceImpl)

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring String Kafka Producer with bootstrap servers: {}", bootstrapServers);

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        addSaslConfig(configProps);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        // This satisfies EventServiceImpl's KafkaTemplate<String,String> dependency
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // 2) ProfileUpdateEventResponse template (for profile update events)

    @Bean
    public ProducerFactory<String, ProfileUpdateEventResponse> profileUpdateProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring ProfileUpdate Kafka Producer with bootstrap servers: {}", bootstrapServers);

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        org.springframework.kafka.support.serializer.JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        addSaslConfig(configProps);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ProfileUpdateEventResponse> profileUpdateKafkaTemplate() {
        return new KafkaTemplate<>(profileUpdateProducerFactory());
    }

    // === CompanyNameResponseEvent producer ===
    @Bean
    public ProducerFactory<String, CompanyNameResponseEvent> companyNameResponseProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring CompanyNameResponse Kafka Producer with bootstrap servers: {}", bootstrapServers);

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                        org.springframework.kafka.support.serializer.JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        addSaslConfig(configProps);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CompanyNameResponseEvent> companyNameResponseKafkaTemplate() {
        return new KafkaTemplate<>(companyNameResponseProducerFactory());
    }
}
