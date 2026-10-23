package com.flowforge.dto.response;

import com.flowforge.entity.User;

public record UserSettingsResponse(
        boolean taskAssigned,
        boolean taskCompleted,
        boolean jobFailed,
        boolean reportReady
) {
    public static UserSettingsResponse from(User user) {
        return new UserSettingsResponse(
                user.isTaskAssignedNotifications(),
                user.isTaskCompletedNotifications(),
                user.isJobFailedNotifications(),
                user.isReportReadyNotifications()
        );
    }
}
