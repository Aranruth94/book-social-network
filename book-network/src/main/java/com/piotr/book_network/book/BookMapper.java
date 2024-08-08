package com.piotr.book_network.book;

import org.springframework.stereotype.Service;

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
                .build();
    }
}
