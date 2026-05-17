package com.linktalk.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"participant_a_id", "participant_b_id"})
})
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_a_id")
    private User participantA;

    @ManyToOne(optional = false)
    @JoinColumn(name = "participant_b_id")
    private User participantB;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant lastMessageAt = Instant.now();

    protected Conversation() {
    }

    public Conversation(User participantA, User participantB) {
        this.participantA = participantA;
        this.participantB = participantB;
    }

    public Long getId() {
        return id;
    }

    public User getParticipantA() {
        return participantA;
    }

    public User getParticipantB() {
        return participantB;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void touch(Instant time) {
        this.lastMessageAt = time;
    }
}
