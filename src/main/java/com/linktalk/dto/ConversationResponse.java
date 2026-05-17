package com.linktalk.dto;

import com.linktalk.model.Conversation;
import com.linktalk.model.Message;
import com.linktalk.model.User;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        Long companionId,
        String companionName,
        Integer companionAge,
        String companionCity,
        String lastMessage,
        Instant lastMessageAt
) {
    public static ConversationResponse from(Conversation conversation, Long viewerId, Message lastMessage) {
        User companion = conversation.getParticipantA().getId().equals(viewerId)
                ? conversation.getParticipantB()
                : conversation.getParticipantA();
        return new ConversationResponse(
                conversation.getId(),
                companion.getId(),
                companion.getFirstName() + (companion.getLastName() == null ? "" : " " + companion.getLastName()),
                companion.getAge(),
                companion.getCity(),
                lastMessage == null ? null : lastMessage.getContent(),
                lastMessage == null ? conversation.getLastMessageAt() : lastMessage.getCreatedAt()
        );
    }
}
