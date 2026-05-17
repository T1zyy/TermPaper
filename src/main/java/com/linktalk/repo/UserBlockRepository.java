package com.linktalk.repo;

import com.linktalk.model.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    @Query("select ub.blocked.id from UserBlock ub where ub.blocker.id = :userId")
    List<Long> findBlockedIdsByBlockerId(@Param("userId") Long userId);

    @Query("select ub.blocker.id from UserBlock ub where ub.blocked.id = :userId")
    List<Long> findBlockerIdsByBlockedId(@Param("userId") Long userId);

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
