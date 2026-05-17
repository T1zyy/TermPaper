package com.linktalk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConversationRequestSendRequest(
        @NotNull Long recipientId,
        @Size(max = 500) String message
) {
}
