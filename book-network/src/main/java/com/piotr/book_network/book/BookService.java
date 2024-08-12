package com.piotr.book_network.book;

import com.piotr.book_network.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {
    Integer saveBook(BookRequest bookRequest, Authentication authentication);

    BookResponse findById(Integer bookId);

    PageResponse<BookResponse> findAllBooks(int page, int size, Authentication authentication);

    PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication authentication);

    PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication authentication);

    PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication authentication);

    Integer updateShareableStatus(Integer bookId, Authentication authentication);

    Integer updateArchivedStatus(Integer bookId, Authentication authentication);

    Integer borrowBook(Integer bookId, Authentication authentication);

    Integer returnBorrowBook(Integer bookId, Authentication authentication);

    Integer approveReturnBorrowBook(Integer bookId, Authentication authentication);

    void uploadBookCoverPicture(MultipartFile file, Authentication authentication, Integer bookId);

    Book findBookById(Integer bookId);

    void isBookArchivedAndNotShareable(Book book, String message);

    void checkBookOwnership(Book book, User user, String message);

    void checkBookNotOwnership(Book book, User user, String message);
}
