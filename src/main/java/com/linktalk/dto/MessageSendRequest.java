package com.linktalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
        @NotBlank @Size(max = 2000) String content
) {
}
