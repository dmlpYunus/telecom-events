package com.argela.telecom_events.service;

import com.argela.telecom_events.api.dto.EventRequest;
import com.argela.telecom_events.config.KafkaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Spring Boot tarafından otomatik yapılandırılır

    public void send(EventRequest request) {
        
        try {
            String payload = objectMapper.writeValueAsString(request);
            String key = request.getSubscriberId();
            
            kafkaTemplate.send(KafkaConfig.EVENTS_TOPIC, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Kafka send failed for subscriberId={} error={}", key, ex.getMessage(), ex);
                        } else if (result != null) {
                            log.info("Kafka sent to {} partition={} offset={} key={}",
                                    KafkaConfig.EVENTS_TOPIC,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    key
                            );
                        }
                    });

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize EventRequest", e);
        }
    }
}
