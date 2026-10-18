package org.BrainWaze.notification.config;

import org.BrainWaze.notification.dto.FinalMatchConfirmedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotificationKafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, FinalMatchConfirmedEvent>
    finalMatchConsumerFactory() {

        JsonDeserializer<FinalMatchConfirmedEvent> jsonDeserializer = new JsonDeserializer<>(FinalMatchConfirmedEvent.class, false);
        jsonDeserializer.addTrustedPackages("org.Acadly.BrainWave.dto", "org.BrainWaze.notification.event");

        ErrorHandlingDeserializer<FinalMatchConfirmedEvent> valueDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "knowrau-notification-final-match");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FinalMatchConfirmedEvent> finalMatchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FinalMatchConfirmedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(finalMatchConsumerFactory());
        return factory;
    }
}