package com.argela.telecom_events.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class EventRequest {

    @NotBlank
    private String subscriberId;

    @NotBlank
    private String type;

    @NotNull
    private Instant timestamp;

    // { "calledNumber": "+905555555555" }
    private Map<String, Object> details;
}
