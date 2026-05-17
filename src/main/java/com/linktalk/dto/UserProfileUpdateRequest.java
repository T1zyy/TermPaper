package com.linktalk.dto;

import com.linktalk.model.Gender;
import jakarta.validation.constraints.*;

import java.util.List;

public record UserProfileUpdateRequest(
        @NotBlank @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Min(16) @Max(120) int age,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 80) String language,
        @NotNull Gender gender,
        @Size(max = 1000) String about,
        @NotNull List<@NotBlank String> interestCodes,
        List<@NotBlank String> goalCodes
) {
}
