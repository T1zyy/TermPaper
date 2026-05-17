package com.linktalk.service;

import com.linktalk.model.Goal;
import com.linktalk.model.Gender;
import com.linktalk.model.Interest;
import com.linktalk.model.RecommendationView;
import com.linktalk.model.User;
import com.linktalk.repo.RecommendationViewRepository;
import com.linktalk.repo.ConversationRequestRepository;
import com.linktalk.repo.PostponedUserRepository;
import com.linktalk.repo.UserBlockRepository;
import com.linktalk.repo.UserRepository;
import com.linktalk.support.RecommendationFilter;
import com.linktalk.support.RecommendationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final RecommendationViewRepository recommendationViewRepository;
    private final PostponedUserRepository postponedUserRepository;
    private final ConversationRequestRepository conversationRequestRepository;
    private final long viewCooldownHours;

    public RecommendationService(UserRepository userRepository,
                                 UserBlockRepository userBlockRepository,
                                 RecommendationViewRepository recommendationViewRepository,
                                 PostponedUserRepository postponedUserRepository,
                                 ConversationRequestRepository conversationRequestRepository,
                                 @Value("${linktalk.recommendations.view-cooldown-hours:24}") long viewCooldownHours) {
        this.userRepository = userRepository;
        this.userBlockRepository = userBlockRepository;
        this.recommendationViewRepository = recommendationViewRepository;
        this.postponedUserRepository = postponedUserRepository;
        this.conversationRequestRepository = conversationRequestRepository;
        this.viewCooldownHours = viewCooldownHours;
    }

    public List<RecommendationResult> recommend(Long viewerId, RecommendationFilter filter) {
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Set<Long> blocked = new HashSet<>(userBlockRepository.findBlockedIdsByBlockerId(viewerId));
        blocked.addAll(userBlockRepository.findBlockerIdsByBlockedId(viewerId));

        Instant since = Instant.now().minus(Duration.ofHours(viewCooldownHours));
        Set<Long> recentlyViewed = new HashSet<>(recommendationViewRepository.findViewedUserIdsSince(viewerId, since));
        Set<Long> postponed = new HashSet<>(postponedUserRepository.findPostponedUserIds(viewerId));

        List<User> candidates = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(viewerId))
                .filter(u -> !blocked.contains(u.getId()))
                .filter(u -> !recentlyViewed.contains(u.getId()))
                .filter(u -> !postponed.contains(u.getId()))
                .filter(u -> !conversationRequestRepository.existsBetween(viewerId, u.getId()))
                .filter(u -> matchesFilter(viewer, u, filter))
                .toList();

        List<RecommendationResult> result = new ArrayList<>();
        for (User candidate : candidates) {
            double score = calculateScore(viewer, candidate);
            result.add(new RecommendationResult(candidate, score));
        }

        result.sort(Comparator
                .comparingDouble(RecommendationResult::score).reversed()
                .thenComparing(r -> r.user().getId()));

        return result;
    }

    public void recordView(Long viewerId, Long viewedUserId) {
        if (viewerId.equals(viewedUserId)) {
            return;
        }
        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        User viewed = userRepository.findById(viewedUserId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        recommendationViewRepository.save(new RecommendationView(viewer, viewed));
    }

    private boolean matchesFilter(User viewer, User candidate, RecommendationFilter filter) {
        if (filter == null) {
            return true;
        }

        if (filter.city() != null) {
            String filterCity = filter.city().trim().toLowerCase();
            String candidateCity = candidate.getCity() == null ? "" : candidate.getCity().trim().toLowerCase();
            if (!filterCity.isEmpty() && !filterCity.equals(candidateCity)) {
                return false;
            }
        }
        if (filter.language() != null && !filter.language().equalsIgnoreCase(candidate.getLanguage())) {
            return false;
        }
        if (filter.ageFrom() != null && candidate.getAge() < filter.ageFrom()) {
            return false;
        }
        if (filter.ageTo() != null && candidate.getAge() > filter.ageTo()) {
            return false;
        }
        if (filter.gender() != null) {
            if (candidate.getGender() == null || filter.gender() != candidate.getGender()) {
                return false;
            }
        }
        if (filter.goalCodes() != null && !filter.goalCodes().isEmpty()) {
            Set<String> candidateGoals = candidate.getGoals().stream()
                    .map(Goal::getCode)
                    .collect(Collectors.toSet());
            if (!candidateGoals.containsAll(filter.goalCodes())) {
                return false;
            }
        }
        if (filter.interestCodes() != null && !filter.interestCodes().isEmpty()) {
            Set<String> candidateInterests = candidate.getInterests().stream()
                    .map(Interest::getCode)
                    .collect(Collectors.toSet());
            if (!candidateInterests.containsAll(filter.interestCodes())) {
                return false;
            }
        }
        if (Boolean.TRUE.equals(filter.commonOnly())) {
            Set<String> viewerInterests = viewer.getInterests().stream()
                    .map(Interest::getCode)
                    .collect(Collectors.toSet());
            boolean hasCommonInterest = candidate.getInterests().stream()
                    .map(Interest::getCode)
                    .anyMatch(viewerInterests::contains);
            if (!hasCommonInterest) {
                return false;
            }
        }
        if (filter.q() != null && !filter.q().trim().isEmpty() && !matchesText(candidate, filter.q())) {
            return false;
        }

        return true;
    }

    private boolean matchesText(User candidate, String query) {
        String normalizedQuery = query.trim().toLowerCase();
        String searchable = String.join(" ",
                nullToEmpty(candidate.getFirstName()),
                nullToEmpty(candidate.getLastName()),
                nullToEmpty(candidate.getCity()),
                nullToEmpty(candidate.getAbout()),
                candidate.getInterests().stream()
                        .map(i -> i.getNameRu() + " " + i.getNameEn())
                        .collect(Collectors.joining(" ")),
                candidate.getGoals().stream()
                        .map(g -> g.getNameRu() + " " + g.getNameEn())
                        .collect(Collectors.joining(" "))
        ).toLowerCase();
        return searchable.contains(normalizedQuery);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private double calculateScore(User viewer, User candidate) {
        Set<Interest> viewerInterests = viewer.getInterests();
        if (viewerInterests.isEmpty()) {
            return 0.0;
        }

        Set<Interest> candidateInterests = candidate.getInterests();
        Set<String> candidateCodes = candidateInterests.stream()
                .map(Interest::getCode)
                .collect(Collectors.toSet());
        Set<String> candidateClusters = candidateInterests.stream()
                .map(i -> i.getCluster().name())
                .collect(Collectors.toSet());

        double base = 100.0 / viewerInterests.size();
        double score = 0.0;

        for (Interest interest : viewerInterests) {
            if (candidateCodes.contains(interest.getCode())) {
                score += base;
            } else if (candidateClusters.contains(interest.getCluster().name())) {
                score += base * 0.5;
            }
        }

        return Math.min(100.0, score);
    }

}
