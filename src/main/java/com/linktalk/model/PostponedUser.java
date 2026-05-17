package com.linktalk.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "postponed_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"owner_id", "postponed_user_id"})
})
public class PostponedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(optional = false)
    @JoinColumn(name = "postponed_user_id")
    private User postponedUser;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected PostponedUser() {
    }

    public PostponedUser(User owner, User postponedUser) {
        this.owner = owner;
        this.postponedUser = postponedUser;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public User getPostponedUser() {
        return postponedUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
