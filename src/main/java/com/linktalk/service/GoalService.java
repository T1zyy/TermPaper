package com.linktalk.service;

import com.linktalk.model.Goal;
import com.linktalk.repo.GoalRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class GoalService {
    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public List<Goal> list() {
        return goalRepository.findAll().stream()
                .sorted(Comparator.comparing(Goal::getCode))
                .toList();
    }
}
