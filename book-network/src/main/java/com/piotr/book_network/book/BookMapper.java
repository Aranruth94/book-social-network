package com.piotr.book_network.book;

import com.piotr.book_network.history.BookTransactionHistory;
import org.springframework.stereotype.Service;

import static com.piotr.book_network.file.FileUtils.readFileFromLocation;

@Service
public class BookMapper {

    public Book mapToBook(BookRequest bookRequest) {
        return Book.builder()
                .id(bookRequest.id())
                .title(bookRequest.title())
                .authorName(bookRequest.authorName())
                .synopsis(bookRequest.synopsis())
                .archived(false)
                .shareable(bookRequest.shareable())
                .build();
    }

    public BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .authorName(book.getAuthorName())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .rate(book.getRate())
                .owner(book.getOwner().getFullName())
                .archived(book.isArchived())
                .shareable(book.isShareable())
                .cover(readFileFromLocation(book.getBookCover()))
                .build();
    }

    public BorrowedBookResponse mapToBorrowedBookResponse(BookTransactionHistory bookTransactionHistory) {
        return BorrowedBookResponse.builder()
                .id(bookTransactionHistory.getBook().getId())
                .title(bookTransactionHistory.getBook().getTitle())
                .authorName(bookTransactionHistory.getBook().getAuthorName())
                .rate(bookTransactionHistory.getBook().getRate())
                .archived(bookTransactionHistory.isReturned())
                .returnApproved(bookTransactionHistory.isReturnApproved())
                .build();
    }
}
