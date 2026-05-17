package com.linktalk.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linktalk.dto.MessageResponse;
import com.linktalk.dto.MessageSendRequest;
import com.linktalk.model.AuthUserDetails;
import com.linktalk.model.Message;
import com.linktalk.service.ConversationService;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByConversation = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> conversationBySession = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ConversationService conversationService, ObjectMapper objectMapper) {
        this.conversationService = conversationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = currentUserId(session);
        Long conversationId = conversationId(session.getUri());
        if (userId == null || conversationId == null || !conversationService.isParticipant(userId, conversationId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not allowed"));
            return;
        }

        sessionsByConversation
                .computeIfAbsent(conversationId, ignored -> ConcurrentHashMap.newKeySet())
                .add(session);
        conversationBySession.put(session.getId(), conversationId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = currentUserId(session);
        Long conversationId = conversationBySession.get(session.getId());
        if (userId == null || conversationId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not allowed"));
            return;
        }

        MessageSendRequest request = objectMapper.readValue(textMessage.getPayload(), MessageSendRequest.class);
        Message message = conversationService.sendMessage(userId, conversationId, request.content());
        String payload = objectMapper.writeValueAsString(MessageResponse.from(message));
        broadcast(conversationId, payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long conversationId = conversationBySession.remove(session.getId());
        if (conversationId == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByConversation.get(conversationId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByConversation.remove(conversationId);
            }
        }
    }

    private void broadcast(Long conversationId, String payload) throws IOException {
        Set<WebSocketSession> sessions = sessionsByConversation.get(conversationId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }

    private Long conversationId(URI uri) {
        if (uri == null) {
            return null;
        }
        String value = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("chatId");
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long currentUserId(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof AbstractAuthenticationToken auth
                && auth.getPrincipal() instanceof AuthUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
