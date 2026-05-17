package com.linktalk.service;

import com.linktalk.model.Conversation;
import com.linktalk.model.Message;
import com.linktalk.model.User;
import com.linktalk.repo.ConversationRepository;
import com.linktalk.repo.MessageRepository;
import com.linktalk.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;

    public ConversationService(ConversationRepository conversationRepository,
                               MessageRepository messageRepository,
                               UserRepository userRepository,
                               BlockService blockService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.blockService = blockService;
    }

    public Conversation createOrGet(Long firstUserId, Long secondUserId) {
        return conversationRepository.findBetween(firstUserId, secondUserId)
                .orElseGet(() -> {
                    User first = userRepository.findById(firstUserId)
                            .orElseThrow(() -> new IllegalStateException("User not found"));
                    User second = userRepository.findById(secondUserId)
                            .orElseThrow(() -> new IllegalStateException("User not found"));
                    User participantA = first.getId() < second.getId() ? first : second;
                    User participantB = first.getId() < second.getId() ? second : first;
                    return conversationRepository.save(new Conversation(participantA, participantB));
                });
    }

    public List<Conversation> list(Long userId) {
        return conversationRepository.findByParticipant(userId).stream()
                .filter(conversation -> !blockService.isBlockedEitherWay(userId, companionId(userId, conversation)))
                .toList();
    }

    public List<Message> messages(Long userId, Long conversationId) {
        requireParticipant(userId, conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public Message lastMessage(Long conversationId) {
        return messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId)
                .orElse(null);
    }

    public Message sendMessage(Long userId, Long conversationId, String content) {
        Conversation conversation = requireParticipant(userId, conversationId);
        if (blockService.isBlockedEitherWay(userId, companionId(userId, conversation))) {
            throw new IllegalArgumentException("User is blocked");
        }
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Message is empty");
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Message message = messageRepository.save(new Message(conversation, sender, trimmed));
        conversation.touch(Instant.now());
        conversationRepository.save(conversation);
        return message;
    }

    public boolean isParticipant(Long userId, Long conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conversation -> isParticipant(userId, conversation))
                .orElse(false);
    }

    private Conversation requireParticipant(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalStateException("Conversation not found"));
        if (!isParticipant(userId, conversation)) {
            throw new IllegalArgumentException("Not allowed");
        }
        return conversation;
    }

    private boolean isParticipant(Long userId, Conversation conversation) {
        return conversation.getParticipantA().getId().equals(userId)
                || conversation.getParticipantB().getId().equals(userId);
    }

    private Long companionId(Long userId, Conversation conversation) {
        return conversation.getParticipantA().getId().equals(userId)
                ? conversation.getParticipantB().getId()
                : conversation.getParticipantA().getId();
    }
}
