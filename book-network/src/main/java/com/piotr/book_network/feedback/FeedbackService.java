package com.piotr.book_network.feedback;

import org.springframework.security.core.Authentication;

public interface FeedbackService {
    Integer save(FeedbackRequest feedbackRequest, Authentication authentication);
}
