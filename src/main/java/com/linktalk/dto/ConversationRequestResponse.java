package com.linktalk.dto;

import com.linktalk.model.ConversationRequest;
import com.linktalk.model.ConversationRequestStatus;

import java.time.Instant;

public record ConversationRequestResponse(
        Long id,
        Long senderId,
        Long recipientId,
        String senderName,
        String recipientName,
        Integer senderAge,
        Integer recipientAge,
        String senderCity,
        String recipientCity,
        String message,
        ConversationRequestStatus status,
        Instant createdAt,
        Instant respondedAt
) {
    public static ConversationRequestResponse from(ConversationRequest request) {
        return new ConversationRequestResponse(
                request.getId(),
                request.getSender().getId(),
                request.getRecipient().getId(),
                fullName(request.getSender().getFirstName(), request.getSender().getLastName()),
                fullName(request.getRecipient().getFirstName(), request.getRecipient().getLastName()),
                request.getSender().getAge(),
                request.getRecipient().getAge(),
                request.getSender().getCity(),
                request.getRecipient().getCity(),
                request.getMessage(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getRespondedAt()
        );
    }

    private static String fullName(String firstName, String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
