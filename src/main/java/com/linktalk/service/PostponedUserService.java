package com.linktalk.service;

import com.linktalk.model.PostponedUser;
import com.linktalk.model.User;
import com.linktalk.repo.PostponedUserRepository;
import com.linktalk.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostponedUserService {
    private final UserRepository userRepository;
    private final PostponedUserRepository postponedUserRepository;

    public PostponedUserService(UserRepository userRepository,
                                PostponedUserRepository postponedUserRepository) {
        this.userRepository = userRepository;
        this.postponedUserRepository = postponedUserRepository;
    }

    public void postpone(Long ownerId, Long postponedUserId) {
        if (ownerId.equals(postponedUserId)) {
            throw new IllegalArgumentException("Cannot postpone yourself");
        }
        if (postponedUserRepository.findByOwnerIdAndPostponedUserId(ownerId, postponedUserId).isPresent()) {
            return;
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        User postponedUser = userRepository.findById(postponedUserId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        postponedUserRepository.save(new PostponedUser(owner, postponedUser));
    }

    public void remove(Long ownerId, Long postponedUserId) {
        postponedUserRepository.findByOwnerIdAndPostponedUserId(ownerId, postponedUserId)
                .ifPresent(postponedUserRepository::delete);
    }

    public List<User> list(Long ownerId) {
        return postponedUserRepository.findByOwnerIdWithUsers(ownerId).stream()
                .map(PostponedUser::getPostponedUser)
                .toList();
    }
}
