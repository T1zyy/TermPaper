package com.linktalk.controller;

import com.linktalk.dto.InterestResponse;
import com.linktalk.service.InterestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
public class InterestController {
    private final InterestService interestService;

    public InterestController(InterestService interestService) {
        this.interestService = interestService;
    }

    @GetMapping
    public List<InterestResponse> list() {
        return interestService.list().stream()
                .map(InterestResponse::from)
                .toList();
    }
}
