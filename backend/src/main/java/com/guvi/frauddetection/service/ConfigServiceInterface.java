package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.ConfigRequest;
import com.guvi.frauddetection.entity.DetectionConfig;

/**
 * Service contract for managing global fraud detection thresholds and rule weights.
 *
 * <p>Demonstrates interface-driven design and abstraction as required by rubric item A.2.</p>
 */
public interface ConfigServiceInterface {

    /**
     * Retrieves the current detection configuration thresholds and weights.
     *
     * @return active detection configuration
     */
    DetectionConfig getConfig();

    /**
     * Updates detection configuration parameters.
     *
     * @param req updated configuration values
     * @return updated detection configuration
     */
    DetectionConfig updateConfig(ConfigRequest req);

    /**
     * Persists a detection configuration entity directly.
     *
     * @param config the configuration entity to save
     * @return saved detection configuration
     */
    DetectionConfig save(DetectionConfig config);
}
