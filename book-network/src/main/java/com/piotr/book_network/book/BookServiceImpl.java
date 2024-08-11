package com.piotr.book_network.book;

import com.piotr.book_network.exception.OperationNotPermittedException;
import com.piotr.book_network.history.BookTransactionHistory;
import com.piotr.book_network.history.BookTransactionHistoryRepository;
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
import java.util.Objects;

import static com.piotr.book_network.book.BookSpecification.withOwnerId;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final BookMapper bookMapper;

    @Override
    public Integer saveBook(BookRequest bookRequest, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book book = mapAndPrepareBook(bookRequest, user);
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
        User user = (User) authentication.getPrincipal();
        Page<Book> books = findDisplayableBooks(page, size, user);
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
        User user = (User) authentication.getPrincipal();
        Page<Book> books = findBooksByOwner(page, size, user);
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
    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<BookTransactionHistory> borrowedBooks = findBorrowedBooks(page, size, user);
        List<BorrowedBookResponse> borrowedBookResponse = borrowedBooks.stream()
                .map(bookMapper::mapToBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                borrowedBookResponse,
                borrowedBooks.getNumber(),
                borrowedBooks.getSize(),
                borrowedBooks.getTotalElements(),
                borrowedBooks.getTotalPages(),
                borrowedBooks.isFirst(),
                borrowedBooks.isLast()
        );
    }

    @Override
    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Page<BookTransactionHistory> returnedBooks = findReturnedBooks(page, size, user);
        List<BorrowedBookResponse> returnedBookResponse = returnedBooks.stream()
                .map(bookMapper::mapToBorrowedBookResponse)
                .toList();
        return new PageResponse<>(
                returnedBookResponse,
                returnedBooks.getNumber(),
                returnedBooks.getSize(),
                returnedBooks.getTotalElements(),
                returnedBooks.getTotalPages(),
                returnedBooks.isFirst(),
                returnedBooks.isLast()
        );
    }

    @Override
    public Integer updateShareableStatus(Integer bookId, Authentication authentication) {
        Book book = findBookById(bookId);
        User user = (User) authentication.getPrincipal();
        checkBookOwnership(book, user);
        book.setShareable(!book.isShareable());
        return saveAndReturnBookId(book);
    }

    @Override
    public Integer updateArchivedStatus(Integer bookId, Authentication authentication) {
        Book book = findBookById(bookId);
        User user = (User) authentication.getPrincipal();
        checkBookOwnership(book, user);
        book.setArchived(!book.isArchived());
        return saveAndReturnBookId(book);
    }

    @Override
    public Integer borrowBook(Integer bookId, Authentication authentication) {
        Book book = findBookById(bookId);
        User user = (User) authentication.getPrincipal();
        isBookArchivedAndNotShareable(book);
        checkBookOwnership(book, user, "You cannot borrow your own book");
        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("You have already borrowed this book");
        }
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    @Override
    public Integer returnBorrowBook(Integer bookId, Authentication authentication) {
        Book book = findBookById(bookId);
        User user = (User) authentication.getPrincipal();
        isBookArchivedAndNotShareable(book);
        checkBookOwnership(book, user, "You cannot borrow or return your own book");
        BookTransactionHistory bookTransactionHistory = findBookTransactionHistoryByBookIdAndUserId(bookId, user.getId());
        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    @Override
    public Integer approveReturnBorrowBook(Integer bookId, Authentication authentication) {
        Book book = findBookById(bookId);
        User user = (User) authentication.getPrincipal();
        isBookArchivedAndNotShareable(book);
        checkBookOwnership(book, user, "You cannot borrow or return your own book");
        BookTransactionHistory bookTransactionHistory = findBookTransactionHistoryByBookIdAndOwnerId(bookId, user.getId());
        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    private Book mapAndPrepareBook(BookRequest bookRequest, User user) {
        Book book = bookMapper.mapToBook(bookRequest);
        book.setOwner(user);
        return book;
    }

    private Integer saveAndReturnBookId(Book book) {
        return bookRepository.save(book).getId();
    }

    private Page<Book> findDisplayableBooks(int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return bookRepository.findAllDisplayableBooks(pageable, user.getId());
    }

    private Page<Book> findBooksByOwner(int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return bookRepository.findAll(withOwnerId(user.getId()), pageable);
    }

    private Page<BookTransactionHistory> findBorrowedBooks(int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return bookTransactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());
    }

    private Page<BookTransactionHistory> findReturnedBooks(int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return bookTransactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());
    }

    private Book findBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    private void checkBookOwnership(Book book, User user) {
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not the owner of this book");
        }
    }

    private void checkBookOwnership(Book book, User user, String message) {
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException(message);
        }
    }

    private void isBookArchivedAndNotShareable(Book book) {
        if (isBookArchived(book) || isBookNotShareable(book)) {
            throw new OperationNotPermittedException("Book cannot be borrowed because it is archived and not shareable");
        }
    }

    private Boolean isBookArchived(Book book) {
        return book.isArchived();
    }

    private Boolean isBookNotShareable(Book book) {
        return !book.isShareable();
    }

    private BookTransactionHistory findBookTransactionHistoryByBookIdAndUserId(Integer bookId, Integer userId) {
        return bookTransactionHistoryRepository.findBookByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this book"));
    }

    private BookTransactionHistory findBookTransactionHistoryByBookIdAndOwnerId(Integer bookId, Integer userId) {
        return bookTransactionHistoryRepository.findBookByIdAndOwnerId(bookId, userId)
                .orElseThrow(() -> new OperationNotPermittedException("The book is not returned yet"));
    }
}