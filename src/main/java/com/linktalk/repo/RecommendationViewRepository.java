package com.linktalk.repo;

import com.linktalk.model.RecommendationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface RecommendationViewRepository extends JpaRepository<RecommendationView, Long> {
    @Query("select rv.viewedUser.id from RecommendationView rv where rv.viewer.id = :viewerId and rv.viewedAt >= :since")
    List<Long> findViewedUserIdsSince(@Param("viewerId") Long viewerId, @Param("since") Instant since);
}
