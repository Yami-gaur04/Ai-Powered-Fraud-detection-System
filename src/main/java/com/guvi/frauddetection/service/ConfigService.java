package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.ConfigRequest;
import com.guvi.frauddetection.entity.DetectionConfig;
import com.guvi.frauddetection.exception.ResourceNotFoundException;
import com.guvi.frauddetection.repository.DetectionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final DetectionConfigRepository configRepository;

    @Transactional(readOnly = true)
    public DetectionConfig getConfig() {
        return configRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("Detection configuration not found"));
    }

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

    @Transactional
    public DetectionConfig save(DetectionConfig config) {
        return configRepository.save(config);
    }
}
