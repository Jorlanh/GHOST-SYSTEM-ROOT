package com.ghost.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ghost_memories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GhostMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "firebase_uid", nullable = false)
    private String firebaseUid;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "importance_weight")
    private Integer importanceWeight = 1;

    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata = "{}";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}