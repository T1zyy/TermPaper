package com.linktalk.controller;

import com.linktalk.dto.ConversationResponse;
import com.linktalk.dto.MessageResponse;
import com.linktalk.model.AuthUserDetails;
import com.linktalk.service.ConversationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ConversationService conversationService;

    public ChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public List<ConversationResponse> list(@AuthenticationPrincipal AuthUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return conversationService.list(principal.getId()).stream()
                .map(conversation -> ConversationResponse.from(
                        conversation,
                        principal.getId(),
                        conversationService.lastMessage(conversation.getId())
                ))
                .toList();
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageResponse> messages(@AuthenticationPrincipal AuthUserDetails principal,
                                          @PathVariable Long conversationId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return conversationService.messages(principal.getId(), conversationId).stream()
                .map(MessageResponse::from)
                .toList();
    }
}
