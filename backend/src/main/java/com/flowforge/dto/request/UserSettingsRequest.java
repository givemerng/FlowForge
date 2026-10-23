package com.flowforge.dto.request;

public record UserSettingsRequest(
        boolean taskAssigned,
        boolean taskCompleted,
        boolean jobFailed,
        boolean reportReady
) {
}
