package com.linktalk.config;

import com.linktalk.model.Goal;
import com.linktalk.model.Gender;
import com.linktalk.model.Interest;
import com.linktalk.model.User;
import com.linktalk.repo.GoalRepository;
import com.linktalk.repo.InterestRepository;
import com.linktalk.repo.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DemoDataSeed implements ApplicationRunner {
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final GoalRepository goalRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeed(UserRepository userRepository,
                        InterestRepository interestRepository,
                        GoalRepository goalRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.interestRepository = interestRepository;
        this.goalRepository = goalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        List<Interest> interests = interestRepository.findAll();
        List<Goal> goals = goalRepository.findAll();

        if (interests.isEmpty()) {
            return;
        }

        List<UserSeed> seeds = List.of(
                new UserSeed("alex.ivanov@mail.com", "Alex", "Ivanov", 22, "Москва", "ru", Gender.MALE,
                        "Люблю живые обсуждения музыки и кино, часто хожу на концерты.",
                        List.of("music", "movies", "books", "sports", "travel"),
                        List.of("friends", "chat")),
                new UserSeed("maria.stepanova@mail.com", "Maria", "Stepanova", 24, "Москва", "ru", Gender.FEMALE,
                        "Занимаюсь дизайном и фотографией, ищу вдохновение и новые идеи.",
                        List.of("design", "photo", "books", "selfdev"),
                        List.of("hobbies", "chat")),
                new UserSeed("pavel.kim@mail.com", "Pavel", "Kim", 21, "Санкт-Петербург", "ru", Gender.MALE,
                        "Интересуюсь ИТ, люблю обсуждать науку и новые гаджеты.",
                        List.of("programming", "science", "gadgets", "videogames"),
                        List.of("network", "practice")),
                new UserSeed("olga.novikova@mail.com", "Olga", "Novikova", 26, "Казань", "ru", Gender.FEMALE,
                        "Путешествую, учу языки и люблю активный отдых на природе.",
                        List.of("travel", "nature", "fitness", "languages"),
                        List.of("friends", "practice")),
                new UserSeed("sergey.petroff@mail.com", "Sergey", "Petrov", 20, "Москва", "ru", Gender.MALE,
                        "Спорт и музыка — мой баланс, всегда за новые знакомства.",
                        List.of("sports", "fitness", "auto", "music"),
                        List.of("chat")),
                new UserSeed("irina.lee@mail.com", "Irina", "Lee", 23, "Санкт-Петербург", "ru", Gender.FEMALE,
                        "Люблю игры, аниме и настолки, ищу компанию по интересам.",
                        List.of("anime", "videogames", "boardgames", "gadgets"),
                        List.of("hobbies")),
                new UserSeed("dmitry.sokolov@mail.com", "Dmitry", "Sokolov", 25, "Новосибирск", "ru", Gender.MALE,
                        "Проекты, стартапы, технологии — готов делиться опытом.",
                        List.of("business", "programming", "science", "selfdev"),
                        List.of("network")),
                new UserSeed("elena.makarova@mail.com", "Elena", "Makarova", 27, "Екатеринбург", "ru", Gender.FEMALE,
                        "Интересуюсь психологией и личным ростом, люблю театр и книги.",
                        List.of("psychology", "selfdev", "books", "theater"),
                        List.of("support", "chat")),
                new UserSeed("nikita.rogov@mail.com", "Nikita", "Rogov", 19, "Москва", "ru", Gender.MALE,
                        "Ищу тех, кто любит игры, кино и музыку так же, как я.",
                        List.of("videogames", "music", "gadgets", "movies"),
                        List.of("chat")),
                new UserSeed("anna.volkova@mail.com", "Anna", "Volkova", 22, "Казань", "ru", Gender.FEMALE,
                        "Фотографирую, путешествую и всегда рада новым знакомствам.",
                        List.of("photo", "design", "travel", "languages"),
                        List.of("friends", "hobbies"))
        );

        String encoded = passwordEncoder.encode("password123");

        for (UserSeed seed : seeds) {
            User user = new User(seed.email(), encoded, seed.firstName(), seed.age(), seed.city(), seed.language(), seed.gender());
            user.setLastName(seed.lastName());
            user.setAbout(seed.about());
            user.setInterests(resolveInterests(interests, seed.interestCodes()));
            user.setGoals(resolveGoals(goals, seed.goalCodes()));
            userRepository.save(user);
        }
    }

    private Set<Interest> resolveInterests(List<Interest> all, List<String> codes) {
        return all.stream()
                .filter(i -> codes.contains(i.getCode()))
                .collect(Collectors.toSet());
    }

    private Set<Goal> resolveGoals(List<Goal> all, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }
        return all.stream()
                .filter(g -> codes.contains(g.getCode()))
                .collect(Collectors.toSet());
    }

    private record UserSeed(
            String email,
            String firstName,
            String lastName,
            int age,
            String city,
            String language,
            Gender gender,
            String about,
            List<String> interestCodes,
            List<String> goalCodes
    ) {
    }
}
