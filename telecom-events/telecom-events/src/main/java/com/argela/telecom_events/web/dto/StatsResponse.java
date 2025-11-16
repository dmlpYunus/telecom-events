package com.argela.telecom_events.web.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatsResponse {

    private long totalEvents;
    private String topServiceType;
    private Map<String, Long> byType;
    private List<MinuteStat> perMinute;
}
