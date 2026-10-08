package com.guvi.frauddetection.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "algorithm_versions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlgorithmVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String versionName;

    @Column(length = 500)
    private String description;

    private Double accuracy;   // precision (%) based on admin-reviewed cases, may be null

    private Boolean active;

    private LocalDateTime createdAt;
}
