package com.linktalk.support;

import com.linktalk.model.User;

public record RecommendationResult(User user, double score) {
}
