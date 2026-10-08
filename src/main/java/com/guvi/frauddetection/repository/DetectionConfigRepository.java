package com.guvi.frauddetection.repository;

import com.guvi.frauddetection.entity.DetectionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetectionConfigRepository extends JpaRepository<DetectionConfig, Long> {
}
