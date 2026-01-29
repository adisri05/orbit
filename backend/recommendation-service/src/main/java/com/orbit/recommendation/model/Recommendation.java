package com.orbit.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private RecommendationType type;
    private String targetId;
    private String title;
    private String reason;
    private double confidence;
    private RuleType ruleApplied;
}

