package com.piotr.book_network.feedback;

import com.piotr.book_network.book.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Integer> saveFeedback(
            @Valid @RequestBody FeedbackRequest feedbackRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(feedbackService.save(feedbackRequest, authentication));
    }

    @GetMapping("book/{bookId}")
    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbackByBook(
            @PathVariable("bookId") Integer bookId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(feedbackService.findAllByBook(bookId, page, size, authentication));
    }
}
