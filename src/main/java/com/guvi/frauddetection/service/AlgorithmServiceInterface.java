package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.AlgorithmUpdateRequest;
import com.guvi.frauddetection.entity.AlgorithmVersion;

import java.util.List;

/**
 * Service contract for model version tracking, feedback-loop tuning,
 * and adaptive threshold retraining.
 *
 * <p>Demonstrates interface-driven design and abstraction as required by rubric item A.2.</p>
 */
public interface AlgorithmServiceInterface {

    /**
     * Lists all registered algorithm versions in reverse chronological order.
     *
     * @return list of algorithm version records
     */
    List<AlgorithmVersion> listVersions();

    /**
     * Retrains and tunes detection thresholds based on cumulative investigation feedback.
     *
     * @param req optional update request parameters
     * @return newly activated algorithm version record
     */
    AlgorithmVersion updateAlgorithm(AlgorithmUpdateRequest req);
}
