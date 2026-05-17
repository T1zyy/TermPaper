package com.linktalk.config;

import com.linktalk.model.Interest;
import com.linktalk.model.InterestCluster;
import com.linktalk.repo.InterestRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InterestSeed implements ApplicationRunner {
    private final InterestRepository interestRepository;

    public InterestSeed(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (interestRepository.count() > 0) {
            return;
        }

        List<Interest> interests = List.of(
                new Interest("music", "Музыка", "Music", InterestCluster.MEDIA),
                new Interest("movies", "Кино и сериалы", "Movies & TV", InterestCluster.MEDIA),
                new Interest("books", "Книги", "Books", InterestCluster.MEDIA),
                new Interest("theater", "Театр и искусство", "Theater & Art", InterestCluster.MEDIA),

                new Interest("videogames", "Видеоигры", "Video games", InterestCluster.GEEK),
                new Interest("boardgames", "Настольные игры", "Board games", InterestCluster.GEEK),
                new Interest("anime", "Аниме и манга", "Anime & Manga", InterestCluster.GEEK),
                new Interest("gadgets", "Технологии и гаджеты", "Tech & Gadgets", InterestCluster.GEEK),

                new Interest("programming", "Программирование и ИТ", "Programming & IT", InterestCluster.PRO),
                new Interest("business", "Бизнес и стартапы", "Business & Startups", InterestCluster.PRO),
                new Interest("science", "Наука", "Science", InterestCluster.PRO),
                new Interest("volunteering", "Волонтерство", "Volunteering", InterestCluster.GROWTH),

                new Interest("psychology", "Психология", "Psychology", InterestCluster.GROWTH),
                new Interest("selfdev", "Саморазвитие", "Self development", InterestCluster.GROWTH),
                new Interest("languages", "Языки", "Languages", InterestCluster.GROWTH),
                new Interest("history", "История", "History", InterestCluster.GROWTH),

                new Interest("sports", "Спорт", "Sports", InterestCluster.ACTIVE),
                new Interest("fitness", "Фитнес и ЗОЖ", "Fitness & Wellness", InterestCluster.ACTIVE),
                new Interest("travel", "Путешествия", "Travel", InterestCluster.ACTIVE),
                new Interest("nature", "Природа и экология", "Nature & Ecology", InterestCluster.ACTIVE),

                new Interest("photo", "Фотография", "Photography", InterestCluster.CREATIVE),
                new Interest("design", "Рисование и дизайн", "Drawing & Design", InterestCluster.CREATIVE),
                new Interest("cooking", "Кулинария", "Cooking", InterestCluster.CREATIVE),
                new Interest("auto", "Авто и мото", "Cars & Moto", InterestCluster.ACTIVE)
        );

        interestRepository.saveAll(interests);
    }
}
