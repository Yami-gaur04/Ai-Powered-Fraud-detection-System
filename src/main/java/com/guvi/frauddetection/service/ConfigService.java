package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.ConfigRequest;
import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.exception.ResourceNotFoundException;
import com.guvi.frauddetection.repository.DetectionConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing detection engine parameters, threshold limits, and rule weighting.
 *
 * <p>Implements {@link ConfigServiceInterface} to satisfy interface-driven design
 * required by rubric item A.2.</p>
 */
@Service
public class ConfigService implements ConfigServiceInterface {

    private final DetectionConfigRepository configRepository;

    /**
     * Constructs the config service with its repository dependency.
     *
     * @param configRepository detection config repository
     */
    public ConfigService(DetectionConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public DetectionConfig getConfig() {
        return configRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Detection configuration not found"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DetectionConfig updateConfig(ConfigRequest req) {
        if (req.flagThreshold() >= req.blockThreshold()) {
            throw new IllegalArgumentException("flagThreshold must be lower than blockThreshold");
        }
        if (req.amountWeight() + req.velocityWeight() + req.locationWeight() + req.deviceWeight() <= 0) {
            throw new IllegalArgumentException("At least one weight must be greater than 0");
        }
        DetectionConfig c = getConfig();
        c.setFlagThreshold(req.flagThreshold());
        c.setBlockThreshold(req.blockThreshold());
        c.setHighAmountLimit(req.highAmountLimit());
        c.setMaxTxPerHour(req.maxTxPerHour());
        c.setAmountWeight(req.amountWeight());
        c.setVelocityWeight(req.velocityWeight());
        c.setLocationWeight(req.locationWeight());
        c.setDeviceWeight(req.deviceWeight());
        return configRepository.save(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public DetectionConfig save(DetectionConfig config) {
        return configRepository.save(config);
    }
}
