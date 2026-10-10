package com.guvi.frauddetection.dto;

/** Both fields are optional; a name is auto-generated if missing. */
public record AlgorithmUpdateRequest(String versionName, String description) {
}
