package com.linktalk.controller;

import com.linktalk.dto.ConversationRequestResponse;
import com.linktalk.dto.ConversationRequestSendRequest;
import com.linktalk.model.AuthUserDetails;
import com.linktalk.service.ConversationRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class ConversationRequestController {
    private final ConversationRequestService conversationRequestService;

    public ConversationRequestController(ConversationRequestService conversationRequestService) {
        this.conversationRequestService = conversationRequestService;
    }

    @PostMapping
    public ConversationRequestResponse send(@AuthenticationPrincipal AuthUserDetails principal,
                                            @Valid @RequestBody ConversationRequestSendRequest request) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        var created = conversationRequestService.send(
                principal.getId(),
                request.recipientId(),
                request.message()
        );
        return ConversationRequestResponse.from(created);
    }

    @GetMapping("/incoming")
    public List<ConversationRequestResponse> incoming(@AuthenticationPrincipal AuthUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return conversationRequestService.incoming(principal.getId()).stream()
                .map(ConversationRequestResponse::from)
                .toList();
    }

    @GetMapping("/outgoing")
    public List<ConversationRequestResponse> outgoing(@AuthenticationPrincipal AuthUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return conversationRequestService.outgoing(principal.getId()).stream()
                .map(ConversationRequestResponse::from)
                .toList();
    }

    @PostMapping("/{requestId}/accept")
    public ConversationRequestResponse accept(@AuthenticationPrincipal AuthUserDetails principal,
                                  @PathVariable Long requestId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return ConversationRequestResponse.from(conversationRequestService.accept(principal.getId(), requestId));
    }

    @PostMapping("/{requestId}/decline")
    public ConversationRequestResponse decline(@AuthenticationPrincipal AuthUserDetails principal,
                                   @PathVariable Long requestId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return ConversationRequestResponse.from(conversationRequestService.decline(principal.getId(), requestId));
    }
}
