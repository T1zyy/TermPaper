package com.linktalk.controller;

import com.linktalk.dto.RecommendationResponse;
import com.linktalk.model.Gender;
import com.linktalk.model.AuthUserDetails;
import com.linktalk.service.RecommendationService;
import com.linktalk.support.RecommendationFilter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RecommendationResponse> list(@AuthenticationPrincipal AuthUserDetails principal,
                                             @RequestParam(required = false) Integer ageFrom,
                                             @RequestParam(required = false) Integer ageTo,
                                             @RequestParam(required = false) String city,
                                             @RequestParam(required = false) String language,
                                             @RequestParam(required = false) Gender gender,
                                             @RequestParam(required = false) List<String> goals,
                                             @RequestParam(required = false) List<String> interests,
                                             @RequestParam(required = false) Boolean commonOnly,
                                             @RequestParam(required = false) String q) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }

        RecommendationFilter filter = new RecommendationFilter(
                ageFrom,
                ageTo,
                city,
                language,
                gender,
                goals,
                interests,
                commonOnly,
                q
        );

        return recommendationService.recommend(principal.getId(), filter).stream()
                .map(RecommendationResponse::from)
                .toList();
    }

    @PostMapping("/{userId}/view")
    public void recordView(@AuthenticationPrincipal AuthUserDetails principal,
                           @PathVariable Long userId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        recommendationService.recordView(principal.getId(), userId);
    }

}
