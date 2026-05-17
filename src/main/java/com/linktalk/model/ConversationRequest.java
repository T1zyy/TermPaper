package com.linktalk.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversation_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "recipient_id", "status"}))
public class ConversationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationRequestStatus status = ConversationRequestStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant respondedAt;

    protected ConversationRequest() {
    }

    public ConversationRequest(User sender, User recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public ConversationRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public void accept() {
        this.status = ConversationRequestStatus.ACCEPTED;
        this.respondedAt = Instant.now();
    }

    public void decline() {
        this.status = ConversationRequestStatus.DECLINED;
        this.respondedAt = Instant.now();
    }
}
