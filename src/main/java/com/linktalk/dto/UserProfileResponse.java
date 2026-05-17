package com.linktalk.dto;

import com.linktalk.model.Gender;
import com.linktalk.model.Interest;
import com.linktalk.model.User;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        int age,
        String city,
        String language,
        Gender gender,
        String about,
        List<String> interests,
        List<String> goals
) {
    public static UserProfileResponse from(User user) {
        List<String> interestCodes = user.getInterests().stream()
                .map(Interest::getCode)
                .sorted()
                .toList();
        List<String> goalCodes = user.getGoals().stream()
                .map(g -> g.getCode())
                .sorted()
                .toList();
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getCity(),
                user.getLanguage(),
                user.getGender(),
                user.getAbout(),
                interestCodes,
                goalCodes
        );
    }
}
