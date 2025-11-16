package com.argela.telecom_events.api;

import com.argela.telecom_events.service.StatsService;
import com.argela.telecom_events.web.dto.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/stats")
    public StatsResponse getStats(@RequestParam(name = "minutes", defaultValue = "5") int minutes) {
        return statsService.getLastMinutesStats(minutes);
    }
}