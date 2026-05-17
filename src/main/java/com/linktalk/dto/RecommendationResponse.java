package com.linktalk.dto;

import com.linktalk.model.Gender;
import com.linktalk.model.Interest;
import com.linktalk.model.User;
import com.linktalk.support.RecommendationResult;

import java.util.List;

public record RecommendationResponse(
        Long id,
        String firstName,
        String lastName,
        int age,
        String city,
        String language,
        Gender gender,
        String about,
        List<String> interests,
        List<String> goals,
        double score
) {
    public static RecommendationResponse from(RecommendationResult recommendation) {
        User user = recommendation.user();
        List<String> interestCodes = user.getInterests().stream()
                .map(Interest::getCode)
                .sorted()
                .toList();
        List<String> goalCodes = user.getGoals().stream()
                .map(g -> g.getCode())
                .sorted()
                .toList();
        return new RecommendationResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getCity(),
                user.getLanguage(),
                user.getGender(),
                user.getAbout(),
                interestCodes,
                goalCodes,
                recommendation.score()
        );
    }
}
