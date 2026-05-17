package com.linktalk.dto;

import com.linktalk.model.Goal;

public record GoalResponse(String code, String nameRu, String nameEn) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(goal.getCode(), goal.getNameRu(), goal.getNameEn());
    }
}
