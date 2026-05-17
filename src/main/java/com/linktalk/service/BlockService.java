package com.linktalk.service;

import com.linktalk.model.User;
import com.linktalk.model.UserBlock;
import com.linktalk.repo.UserBlockRepository;
import com.linktalk.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockService {
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;

    public BlockService(UserRepository userRepository, UserBlockRepository userBlockRepository) {
        this.userRepository = userRepository;
        this.userBlockRepository = userBlockRepository;
    }

    public void block(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("Cannot block yourself");
        }

        userBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Already blocked");
                });

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        userBlockRepository.save(new UserBlock(blocker, blocked));
    }

    public void unblock(Long blockerId, Long blockedId) {
        userBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .ifPresent(userBlockRepository::delete);
    }

    public List<Long> listBlockedIds(Long blockerId) {
        return userBlockRepository.findBlockedIdsByBlockerId(blockerId);
    }

    public boolean isBlockedEitherWay(Long a, Long b) {
        return userBlockRepository.findByBlockerIdAndBlockedId(a, b).isPresent()
                || userBlockRepository.findByBlockerIdAndBlockedId(b, a).isPresent();
    }
}
