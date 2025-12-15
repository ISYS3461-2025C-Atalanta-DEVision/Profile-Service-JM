package com.devision.jm.profile.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration
 *
 * Configures Kafka consumer for receiving events from Auth Service.
 * Discovers Kafka address from Eureka via KafkaDiscoveryService.
 * Only loaded when kafka.enabled=true (KAFKA_ENABLED env var).
 *
 * Implements Microservice Architecture (A.3.2):
 * - Communication among microservices via Message Broker (Kafka)
 *
 * Discovery Flow:
 * 1. KafkaDiscoveryService queries Eureka for kafka-registrar
 * 2. Reads kafkaBroker from metadata
 * 3. Falls back to application.yml if discovery fails
 */
@Slf4j
@Configuration
@EnableKafka
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaConfig {

    private final KafkaDiscoveryService kafkaDiscoveryService;

    @Value("${spring.kafka.consumer.group-id:profile-service-group}")
    private String groupId;

    @Value("${kafka.listener.auto-startup:true}")
    private boolean autoStartup;

    @PostConstruct
    public void init() {
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("========== KAFKA CONFIG LOADED (EUREKA DISCOVERY) ==========");
        log.info("Kafka bootstrap servers: {}", bootstrapServers);
        log.info("Kafka consumer group: {}", groupId);
        log.info("=============================================================");
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Get Kafka address from Eureka discovery
        String bootstrapServers = kafkaDiscoveryService.getKafkaBootstrapServers();
        log.info("Configuring Kafka Consumer with bootstrap servers: {}", bootstrapServers);

        // Kafka broker addresses (discovered from Eureka)
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer group ID
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Key and value deserializers
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Auto offset reset (start from earliest if no offset found)
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Enable auto commit
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        // Connection retry settings - don't fail fast if broker unavailable
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 5000);
        configProps.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 5000);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Don't fail on startup if Kafka is unavailable - retry connection
        factory.getContainerProperties().setMissingTopicsFatal(false);
        factory.getContainerProperties().setAuthExceptionRetryInterval(java.time.Duration.ofSeconds(10));

        // Allow disabling auto-startup via KAFKA_LISTENER_AUTO_STARTUP=false
        // This lets the service start even when Kafka broker is unavailable
        factory.setAutoStartup(autoStartup);

        // Error handler with retry backoff
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(5000L, 3L)); // 5 second interval, 3 retries
        factory.setCommonErrorHandler(errorHandler);

        log.info("Kafka listener auto-startup: {}", autoStartup);
        return factory;
    }
}
