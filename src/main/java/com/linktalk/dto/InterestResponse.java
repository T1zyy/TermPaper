package com.linktalk.dto;

import com.linktalk.model.Interest;

public record InterestResponse(String code, String nameRu, String nameEn, String cluster) {
    public static InterestResponse from(Interest interest) {
        return new InterestResponse(
                interest.getCode(),
                interest.getNameRu(),
                interest.getNameEn(),
                interest.getCluster().name()
        );
    }
}
