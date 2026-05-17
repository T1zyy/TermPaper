package com.linktalk.dto;

import com.linktalk.model.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AuthRegisterCommand(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotBlank @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        int age,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 80) String language,
        @NotNull Gender gender,
        @Size(max = 1000) String about,
        @NotNull List<@NotBlank String> interestCodes,
        List<@NotBlank String> goalCodes
) {
}
