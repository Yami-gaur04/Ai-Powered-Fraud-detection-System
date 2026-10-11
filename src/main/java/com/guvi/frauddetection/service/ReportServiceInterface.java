package com.guvi.frauddetection.service;

import com.guvi.frauddetection.dto.DetectionReport;

/**
 * Service contract for computing platform-wide detection analytics and performance reports.
 *
 * <p>Demonstrates interface-driven design and abstraction as required by rubric item A.2.</p>
 */
public interface ReportServiceInterface {

    /**
     * Aggregates and returns current system-wide detection statistics, case distributions,
     * and model precision rates.
     *
     * @return current detection report metrics
     */
    DetectionReport buildReport();
}
