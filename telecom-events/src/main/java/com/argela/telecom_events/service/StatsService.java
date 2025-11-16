package com.argela.telecom_events.service;

import com.argela.telecom_events.web.dto.MinuteStat;
import com.argela.telecom_events.web.dto.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StringRedisTemplate redis;

    private static final DateTimeFormatter MINUTE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    public void updateStats(String type, Instant timestamp) {

        long minuteBucket = timestamp.getEpochSecond() / 60;
        String totalKey = "stats:minute:" + minuteBucket + ":total";            // stats:minute:29384480:total  -> Ex: 42
        String typeHashKey = "stats:minute:" + minuteBucket + ":types";         // stats:minute:29384480:types  -> Ex: {"CALL": 20, "SMS": 15, ...}

        redis.opsForValue().increment(totalKey);                                // (stats:minute:29123456:total) = "42" -> "43"
        redis.opsForHash().increment(typeHashKey, type, 1);              // (stats:minute:29123456:types) = {"CALL": 20, "SMS": 15} -> {"CALL": 21, "SMS": 15}
    }

    public StatsResponse getLastMinutesStats(int minutes) {

        if (minutes <= 0) minutes = 5;
        if (minutes > 60) minutes = 60; // hard limit

        long nowMinute = Instant.now().getEpochSecond() / 60;
    
        long total = 0;                                     // Toplam event sayısı
        Map<String, Long> typeAgg = new HashMap<>();        // Type bazlı toplamlar
        List<MinuteStat> perMinute = new ArrayList<>();     // Dakika bazlı toplamlar

        for (long m = nowMinute - (minutes - 1); m <= nowMinute; m++) {                   //Ex: 23.55 - 23.59 ? (minutes=5)
            String totalKey = "stats:minute:" + m + ":total";                             // stats:minute:29384480:total  
            String typeHashKey = "stats:minute:" + m + ":types";                          // stats:minute:29384480:types

            String totalStr = redis.opsForValue().get(totalKey);                          // Ex: "42"
            long minuteCount = (totalStr != null) ? Long.parseLong(totalStr) : 0L;           
            if (minuteCount > 0) {
                Map<Object, Object> entries = redis.opsForHash().entries(typeHashKey);      // Ex: {"CALL": 20, "SMS": 15, ...}
                for (Map.Entry<Object, Object> e : entries.entrySet()) {       
                    String t = String.valueOf(e.getKey());                                  // type (field) ex: "CALL"       
                    long c = Long.parseLong(String.valueOf(e.getValue()));                  // count ex: "20"
                    typeAgg.merge(t, c, Long::sum);                  
                }
            }

            total += minuteCount;
            perMinute.add(new MinuteStat( 
                    MINUTE_FMT.format(Instant.ofEpochSecond(m * 60)),   
                    minuteCount 
            ));
        }

        // En çok kullanılan type
        String topType = null;
        long topCount = 0;
        for (Map.Entry<String, Long> e : typeAgg.entrySet()) {
            if (e.getValue() > topCount) {
                topType = e.getKey();
                topCount = e.getValue();
            }
        }

        StatsResponse resp = new StatsResponse();
        resp.setTotalEvents(total);
        resp.setByType(typeAgg);
        resp.setPerMinute(perMinute);
        resp.setTopServiceType(topType);

        return resp;
    }
}
