package com.argela.telecom_events.api;

import com.argela.telecom_events.api.dto.EventRequest;
import com.argela.telecom_events.service.EventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventProducer eventProducer;

    @PostMapping
    public ResponseEntity<Void> publishEvent(@Valid @RequestBody EventRequest request) {
        eventProducer.send(request);
        return ResponseEntity.accepted().build();
    }
}

