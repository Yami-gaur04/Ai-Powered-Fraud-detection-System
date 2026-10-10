package com.guvi.frauddetection.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "algorithm_versions")
public class AlgorithmVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String versionName;

    @Column(length = 500)
    private String description;

    private Double accuracy;   // precision (%) based on admin-reviewed cases, may be null

    private Boolean active;

    private LocalDateTime createdAt;

    public AlgorithmVersion() {}

    public AlgorithmVersion(Long id, String versionName, String description, Double accuracy,
                            Boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.versionName = versionName;
        this.description = description;
        this.accuracy = accuracy;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String versionName;
        private String description;
        private Double accuracy;
        private Boolean active;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder versionName(String versionName) { this.versionName = versionName; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder accuracy(Double accuracy) { this.accuracy = accuracy; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public AlgorithmVersion build() {
            return new AlgorithmVersion(id, versionName, description, accuracy, active, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
