package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
@Data
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    private Long userId;

    @Column(length = 500)
    private String jwtToken;

    private LocalDateTime loginTime = LocalDateTime.now();
    private LocalDateTime expiryTime;
    private Boolean isActive = true;
}