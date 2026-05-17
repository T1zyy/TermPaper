package com.linktalk.repo;

import com.linktalk.model.ConversationRequest;
import com.linktalk.model.ConversationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRequestRepository extends JpaRepository<ConversationRequest, Long> {
    @Query("select cr from ConversationRequest cr where cr.recipient.id = :recipientId order by cr.createdAt desc")
    List<ConversationRequest> findIncoming(@Param("recipientId") Long recipientId);

    @Query("select cr from ConversationRequest cr where cr.sender.id = :senderId order by cr.createdAt desc")
    List<ConversationRequest> findOutgoing(@Param("senderId") Long senderId);

    @Query("select cr from ConversationRequest cr where ((cr.sender.id = :a and cr.recipient.id = :b) or (cr.sender.id = :b and cr.recipient.id = :a)) and cr.status = :status")
    Optional<ConversationRequest> findBetweenWithStatus(@Param("a") Long a,
                                                        @Param("b") Long b,
                                                        @Param("status") ConversationRequestStatus status);

    @Query("select case when count(cr) > 0 then true else false end from ConversationRequest cr where (cr.sender.id = :a and cr.recipient.id = :b) or (cr.sender.id = :b and cr.recipient.id = :a)")
    boolean existsBetween(@Param("a") Long a, @Param("b") Long b);
}
