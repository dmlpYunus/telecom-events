package com.argela.telecom_events.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MinuteStat {
    private String minute;
    private long count;
}
