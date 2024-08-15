package com.piotr.book_network.feedback;

import com.piotr.book_network.book.Book;
import com.piotr.book_network.book.BookService;
import com.piotr.book_network.book.PageResponse;
import com.piotr.book_network.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public PageResponse<FeedbackResponse> findAllByBook(Integer bookId, int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<Feedback> feedbacks = findAllFeedbackByBook(bookId, page, size);
        List<FeedbackResponse> feedbackResponseList = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();
        return new PageResponse<>(
                feedbackResponseList,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }

    private Page<Feedback> findAllFeedbackByBook(Integer bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return feedbackRepository.findAllByBookId(bookId, pageable);
    }
}
