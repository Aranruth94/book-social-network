package com.piotr.book_network.feedback;

import jakarta.validation.constraints.*;

public record FeedbackRequest(

        @Positive(message = "200")
        @Min(value = 0, message = "201")
        @Max(value = 5, message = "202")
        Double note,

        @NotBlank(message = "203")
        String comment,

        @NotNull(message = "204")
        Integer bookId
) {
}
