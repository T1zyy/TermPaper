package com.linktalk.repo;

import com.linktalk.model.PostponedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostponedUserRepository extends JpaRepository<PostponedUser, Long> {
    Optional<PostponedUser> findByOwnerIdAndPostponedUserId(Long ownerId, Long postponedUserId);

    @Query("select pu.postponedUser.id from PostponedUser pu where pu.owner.id = :ownerId")
    List<Long> findPostponedUserIds(@Param("ownerId") Long ownerId);

    @Query("select pu from PostponedUser pu join fetch pu.postponedUser where pu.owner.id = :ownerId order by pu.createdAt desc")
    List<PostponedUser> findByOwnerIdWithUsers(@Param("ownerId") Long ownerId);
}
