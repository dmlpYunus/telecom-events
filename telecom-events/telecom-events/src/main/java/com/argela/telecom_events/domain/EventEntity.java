package com.argela.telecom_events.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subscriberId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Instant timestamp;

    @Lob
    @Column(name = "details_json")
    private String detailsJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
