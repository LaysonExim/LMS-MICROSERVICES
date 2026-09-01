// Purpose: Kafka consumer configuration
// File: notification-service/src/main/java/com/nextgenloan/notification/config/KafkaConsumerConfig.java

package com.nextgenloan.notification_service.config;

import com.nextgenloan.notification_service.event.LoanEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, LoanEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Auto offset reset - if no offset, start from beginning
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Enable auto commit
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        // Max poll records
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(LoanEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LoanEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LoanEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}