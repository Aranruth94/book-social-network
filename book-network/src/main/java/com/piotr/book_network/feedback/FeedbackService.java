package com.piotr.book_network.feedback;

import com.piotr.book_network.book.PageResponse;
import org.springframework.security.core.Authentication;

public interface FeedbackService {
    Integer save(FeedbackRequest feedbackRequest, Authentication authentication);

    PageResponse<FeedbackResponse> findAllByBook(Integer bookId, int page, int size, Authentication authentication);
}
