package com.flowforge.dto.response;

import java.util.List;

public record RiskAnalysisResponse(
        Long taskId,
        String risk,
        Integer riskScore,
        List<String> reasons,
        String calculatedAt
) {
}
