package com.linktalk.config;

import com.linktalk.model.Goal;
import com.linktalk.repo.GoalRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoalSeed implements ApplicationRunner {
    private final GoalRepository goalRepository;

    public GoalSeed(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (goalRepository.count() > 0) {
            return;
        }

        List<Goal> goals = List.of(
                new Goal("friends", "Новые друзья", "New friends"),
                new Goal("chat", "Просто общение", "Casual chat"),
                new Goal("hobbies", "Обсуждение хобби", "Hobby talk"),
                new Goal("practice", "Практика языка", "Language practice"),
                new Goal("network", "Нетворкинг", "Networking"),
                new Goal("support", "Поддержка и обмен опытом", "Support & experience")
        );

        goalRepository.saveAll(goals);
    }
}
