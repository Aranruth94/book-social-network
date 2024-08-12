package com.piotr.book_network.feedback;

import com.piotr.book_network.book.Book;

public class FeedbackMapper {

    public Feedback toFeedback(FeedbackRequest feedbackRequest) {
        return Feedback.builder()
                .note(feedbackRequest.note())
                .comment(feedbackRequest.comment())
                .book(Book.builder()
                        .id(feedbackRequest.bookId())
                        .archived(false)
                        .shareable(false)
                        .build())
                .build();
    }
}
