package com.piotr.book_network.feedback;

import com.piotr.book_network.book.Book;
import com.piotr.book_network.book.BookService;
import com.piotr.book_network.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final BookService bookService;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;

    @Override
    public Integer save(FeedbackRequest feedbackRequest, Authentication authentication) {
        Book book = bookService.findBookById(feedbackRequest.bookId());
        User user = (User) authentication.getPrincipal();
        bookService.isBookArchivedAndNotShareable(book, "You cannot leave feedback for archived or not shareable book");
        bookService.checkBookNotOwnership(book, user, "You cannot give feedback to your own book");
        Feedback feedback = feedbackMapper.toFeedback(feedbackRequest);
        return feedbackRepository.save(feedback).getId();
    }
}
