package com.flowforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        @NotNull Long projectId,
        @NotBlank String type
) {
}
