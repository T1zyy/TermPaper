package com.linktalk.service;

import com.linktalk.dto.AuthRegisterCommand;
import com.linktalk.dto.UserProfileUpdateRequest;
import com.linktalk.model.Goal;
import com.linktalk.model.Interest;
import com.linktalk.model.User;
import com.linktalk.repo.GoalRepository;
import com.linktalk.repo.InterestRepository;
import com.linktalk.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final GoalRepository goalRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       InterestRepository interestRepository,
                       GoalRepository goalRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.interestRepository = interestRepository;
        this.goalRepository = goalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(AuthRegisterCommand command) {
        userRepository.findByEmail(command.email()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use");
        });

        if (command.interestCodes().size() < 3) {
            throw new IllegalArgumentException("Select at least 3 interests");
        }

        Set<Interest> interests = new HashSet<>();
        for (String code : command.interestCodes()) {
            Interest interest = interestRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown interest: " + code));
            interests.add(interest);
        }

        Set<Goal> goals = new HashSet<>();
        if (command.goalCodes() != null) {
            for (String code : command.goalCodes()) {
                Goal goal = goalRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown goal: " + code));
                goals.add(goal);
            }
        }

        User user = new User(
                command.email().trim().toLowerCase(),
                passwordEncoder.encode(command.password()),
                command.firstName().trim(),
                command.age(),
                command.city().trim(),
                command.language().trim(),
                command.gender()
        );
        user.setLastName(trimToNull(command.lastName()));
        user.setAbout(trimToNull(command.about()));
        user.setInterests(interests);
        user.setGoals(goals);

        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public User updateProfile(Long userId, UserProfileUpdateRequest request) {
        if (request.interestCodes().size() < 3) {
            throw new IllegalArgumentException("Select at least 3 interests");
        }

        User user = getById(userId);
        Set<Interest> interests = resolveInterests(request.interestCodes());
        Set<Goal> goals = resolveGoals(request.goalCodes());

        user.setFirstName(request.firstName().trim());
        user.setLastName(trimToNull(request.lastName()));
        user.setAge(request.age());
        user.setCity(request.city().trim());
        user.setLanguage(request.language().trim());
        user.setGender(request.gender());
        user.setAbout(trimToNull(request.about()));
        user.setInterests(interests);
        user.setGoals(goals);

        return userRepository.save(user);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Set<Interest> resolveInterests(List<String> interestCodes) {
        Set<Interest> interests = new HashSet<>();
        for (String code : interestCodes) {
            Interest interest = interestRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown interest: " + code));
            interests.add(interest);
        }
        return interests;
    }

    private Set<Goal> resolveGoals(List<String> goalCodes) {
        Set<Goal> goals = new HashSet<>();
        if (goalCodes != null) {
            for (String code : goalCodes) {
                Goal goal = goalRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown goal: " + code));
                goals.add(goal);
            }
        }
        return goals;
    }

}
