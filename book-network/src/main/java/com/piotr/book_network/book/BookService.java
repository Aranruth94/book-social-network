package com.piotr.book_network.book;

import org.springframework.security.core.Authentication;

public interface BookService {
    Integer saveBook(BookRequest bookRequest, Authentication authentication);

    BookResponse findById(Integer bookId);

    PageResponse<BookResponse> findAllBooks(int page, int size, Authentication authentication);

    PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication authentication);

    PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication authentication);

    PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication authentication);
}
