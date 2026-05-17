package com.linktalk.dto;

import com.linktalk.model.Message;

import java.time.Instant;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        String content,
        Instant createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getSender().getFirstName(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
