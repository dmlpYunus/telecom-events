package com.argela.telecom_events.service;


import com.argela.telecom_events.api.dto.EventRequest;
import com.argela.telecom_events.config.KafkaConfig;
import com.argela.telecom_events.domain.EventEntity;
import com.argela.telecom_events.domain.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final ObjectMapper objectMapper;
    private final EventRepository eventRepository;
    private final StringRedisTemplate redis;
    private final StatsService statsService;

    @KafkaListener(
            topics = KafkaConfig.EVENTS_TOPIC,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message) {

        try {
            EventRequest request = objectMapper.readValue(message, EventRequest.class);

            // [1] MySQL'e kaydet
            EventEntity entity = new EventEntity();
            entity.setSubscriberId(request.getSubscriberId());
            entity.setType(request.getType());
            entity.setTimestamp(request.getTimestamp());
            entity.setDetailsJson(objectMapper.writeValueAsString(request.getDetails()));
            eventRepository.save(entity);

            // [2] Redis - ilgili abonenin son event'leri
            String key = "subscriber:" + request.getSubscriberId() + ":last";
            redis.opsForList().leftPush(key, message);                          // yeni event'i başa ekle. LIFO index=0
            redis.opsForList().trim(key, 0, 99);                     // son 100

            // [3] Redis - istatistik bucket --update
            statsService.updateStats(request.getType(), request.getTimestamp());

            log.info("Consumed event for subscriberId={} type={}", request.getSubscriberId(), request.getType());

        } catch (Exception e) {
            log.error("Failed to consume message: {}", message, e);
        }
    }
}
