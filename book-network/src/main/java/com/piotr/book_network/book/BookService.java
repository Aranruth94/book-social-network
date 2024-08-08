package com.piotr.book_network.book;

import org.springframework.security.core.Authentication;

public interface BookService {
    Integer saveBook(BookRequest bookRequest, Authentication authentication);

    BookResponse findById(Integer bookId);

    PageResponse<BookResponse> findAllBooks(int page, int size, Authentication authentication);
}
