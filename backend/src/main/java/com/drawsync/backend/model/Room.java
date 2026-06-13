package com.drawsync.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    private String roomId; // like ABC123

    private String hostEmail;
    @ElementCollection(fetch = FetchType.EAGER)
    private java.util.List<UserInfo> users = new java.util.ArrayList<>();

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String boardData; // store JSON as string
}