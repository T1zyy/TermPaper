package com.linktalk.controller;

import com.linktalk.dto.PublicUserResponse;
import com.linktalk.model.AuthUserDetails;
import com.linktalk.service.PostponedUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postponed")
public class PostponedUserController {
    private final PostponedUserService postponedUserService;

    public PostponedUserController(PostponedUserService postponedUserService) {
        this.postponedUserService = postponedUserService;
    }

    @GetMapping
    public List<PublicUserResponse> list(@AuthenticationPrincipal AuthUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return postponedUserService.list(principal.getId()).stream()
                .map(PublicUserResponse::from)
                .toList();
    }

    @PostMapping("/{userId}")
    public void postpone(@AuthenticationPrincipal AuthUserDetails principal,
                         @PathVariable Long userId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        postponedUserService.postpone(principal.getId(), userId);
    }

    @DeleteMapping("/{userId}")
    public void remove(@AuthenticationPrincipal AuthUserDetails principal,
                       @PathVariable Long userId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        postponedUserService.remove(principal.getId(), userId);
    }
}
