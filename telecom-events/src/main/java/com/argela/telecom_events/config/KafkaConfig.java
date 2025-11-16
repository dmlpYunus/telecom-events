package com.argela.telecom_events.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String EVENTS_TOPIC = "telecom.events";

    @Bean
    public NewTopic eventsTopic() {
        // 3 partition, replication factor 1 (local ortam için)
        return new NewTopic(EVENTS_TOPIC, 3, (short) 1);
    }
}