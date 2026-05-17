package com.linktalk.repo;

import com.linktalk.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("select c from Conversation c where (c.participantA.id = :a and c.participantB.id = :b) or (c.participantA.id = :b and c.participantB.id = :a)")
    Optional<Conversation> findBetween(@Param("a") Long a, @Param("b") Long b);

    @Query("select c from Conversation c where c.participantA.id = :userId or c.participantB.id = :userId order by c.lastMessageAt desc")
    List<Conversation> findByParticipant(@Param("userId") Long userId);
}
