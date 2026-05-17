package com.linktalk.dto;

import com.linktalk.model.Gender;
import com.linktalk.dto.AuthRegisterCommand;
import jakarta.validation.constraints.*;

import java.util.List;

public record AuthRegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 72) String password,
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
    public AuthRegisterCommand toCommand() {
        return new AuthRegisterCommand(
                email,
                password,
                firstName,
                lastName,
                age,
                city,
                language,
                gender,
                about,
                interestCodes,
                goalCodes
        );
    }
}
