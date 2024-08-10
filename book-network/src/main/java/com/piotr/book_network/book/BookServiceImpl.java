package com.piotr.book_network.book;

import com.piotr.book_network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.piotr.book_network.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public Integer saveBook(BookRequest bookRequest, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Book book = mapAndPrepareBook(bookRequest, currentUser);
        return saveAndReturnBookId(book);
    }

    @Override
    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::mapToBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    @Override
    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Page<Book> books = findDisplayableBooks(page, size, currentUser);
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::mapToBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    @Override
    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Page<Book> books = findBooksByOwner(page, size, currentUser);
        List<BookResponse> bookResponse = books.stream()
                .map(bookMapper::mapToBookResponse)
                .toList();
        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    private Book mapAndPrepareBook(BookRequest bookRequest, User currentUser) {
        Book book = bookMapper.mapToBook(bookRequest);
        book.setOwner(currentUser);
        return book;
    }

    private Integer saveAndReturnBookId(Book book) {
        return bookRepository.save(book).getId();
    }

    private Page<Book> findDisplayableBooks(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return bookRepository.findAllDisplayableBooks(pageable, currentUser.getId());
    }

    private Page<Book> findBooksByOwner(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return bookRepository.findAll(withOwnerId(currentUser.getId()), pageable);
    }
}