package com.guvi.frauddetection.repository;

import com.guvi.frauddetection.entity.AlgorithmVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlgorithmVersionRepository extends JpaRepository<AlgorithmVersion, Long> {
    Optional<AlgorithmVersion> findByActiveTrue();
    List<AlgorithmVersion> findAllByOrderByCreatedAtDesc();
}
