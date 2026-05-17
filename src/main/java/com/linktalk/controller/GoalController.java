package com.linktalk.controller;

import com.linktalk.dto.GoalResponse;
import com.linktalk.service.GoalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public List<GoalResponse> list() {
        return goalService.list().stream()
                .map(GoalResponse::from)
                .toList();
    }
}
