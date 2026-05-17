package com.linktalk.service;

import com.linktalk.model.ConversationRequest;
import com.linktalk.model.ConversationRequestStatus;
import com.linktalk.model.User;
import com.linktalk.repo.ConversationRequestRepository;
import com.linktalk.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationRequestService {
    private final ConversationRequestRepository conversationRequestRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;
    private final ConversationService conversationService;

    public ConversationRequestService(ConversationRequestRepository conversationRequestRepository,
                                      UserRepository userRepository,
                                      BlockService blockService,
                                      ConversationService conversationService) {
        this.conversationRequestRepository = conversationRequestRepository;
        this.userRepository = userRepository;
        this.blockService = blockService;
        this.conversationService = conversationService;
    }

    public ConversationRequest send(Long senderId, Long recipientId, String message) {
        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("Cannot send request to yourself");
        }
        if (blockService.isBlockedEitherWay(senderId, recipientId)) {
            throw new IllegalArgumentException("User is blocked");
        }

        conversationRequestRepository.findBetweenWithStatus(senderId, recipientId, ConversationRequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Request already pending");
                });

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        ConversationRequest request = new ConversationRequest(sender, recipient, trimToNull(message));
        return conversationRequestRepository.save(request);
    }

    public List<ConversationRequest> incoming(Long userId) {
        return conversationRequestRepository.findIncoming(userId).stream()
                .filter(request -> !blockService.isBlockedEitherWay(userId, request.getSender().getId()))
                .filter(request -> request.getStatus() == com.linktalk.model.ConversationRequestStatus.PENDING)
                .toList();
    }

    public List<ConversationRequest> outgoing(Long userId) {
        return conversationRequestRepository.findOutgoing(userId).stream()
                .filter(request -> !blockService.isBlockedEitherWay(userId, request.getRecipient().getId()))
                .toList();
    }

    public ConversationRequest accept(Long userId, Long requestId) {
        ConversationRequest request = conversationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Request not found"));
        if (!request.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        if (request.getStatus() != ConversationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request already processed");
        }
        request.accept();
        ConversationRequest saved = conversationRequestRepository.save(request);
        conversationService.createOrGet(request.getSender().getId(), request.getRecipient().getId());
        return saved;
    }

    public ConversationRequest decline(Long userId, Long requestId) {
        ConversationRequest request = conversationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Request not found"));
        if (!request.getRecipient().getId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        if (request.getStatus() != ConversationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request already processed");
        }
        request.decline();
        return conversationRequestRepository.save(request);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
