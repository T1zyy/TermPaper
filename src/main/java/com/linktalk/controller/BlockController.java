package com.linktalk.controller;

import com.linktalk.model.AuthUserDetails;
import com.linktalk.service.BlockService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {
    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{userId}")
    public void block(@AuthenticationPrincipal AuthUserDetails principal,
                      @PathVariable Long userId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        blockService.block(principal.getId(), userId);
    }

    @DeleteMapping("/{userId}")
    public void unblock(@AuthenticationPrincipal AuthUserDetails principal,
                        @PathVariable Long userId) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        blockService.unblock(principal.getId(), userId);
    }

    @GetMapping
    public List<Long> list(@AuthenticationPrincipal AuthUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        return blockService.listBlockedIds(principal.getId());
    }
}
