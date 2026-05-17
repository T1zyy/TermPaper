package com.linktalk.support;

import com.linktalk.model.Gender;

import java.util.List;

public record RecommendationFilter(
        Integer ageFrom,
        Integer ageTo,
        String city,
        String language,
        Gender gender,
        List<String> goalCodes,
        List<String> interestCodes,
        Boolean commonOnly,
        String q
) {
}
