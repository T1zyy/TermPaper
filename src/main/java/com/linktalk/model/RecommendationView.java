package com.linktalk.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "recommendation_views", indexes = {
        @Index(name = "idx_reco_viewer_time", columnList = "viewer_id, viewedAt")
})
public class RecommendationView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "viewer_id")
    private User viewer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "viewed_user_id")
    private User viewedUser;

    @Column(nullable = false)
    private Instant viewedAt = Instant.now();

    protected RecommendationView() {
    }

    public RecommendationView(User viewer, User viewedUser) {
        this.viewer = viewer;
        this.viewedUser = viewedUser;
    }

    public Long getId() {
        return id;
    }

    public User getViewer() {
        return viewer;
    }

    public User getViewedUser() {
        return viewedUser;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }
}
