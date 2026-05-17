package com.linktalk.service;

import com.linktalk.model.Interest;
import com.linktalk.repo.InterestRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class InterestService {
    private final InterestRepository interestRepository;

    public InterestService(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    public List<Interest> list() {
        return interestRepository.findAll().stream()
                .sorted(Comparator.comparing(Interest::getCode))
                .toList();
    }
}
