package com.piotr.book_network.book;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
@Tag(name = "Book")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<Integer> saveBook(
            @Valid @RequestBody BookRequest bookRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(bookService.saveBook(bookRequest, authentication));
    }

    @GetMapping("/{book-id}")
    public ResponseEntity<BookResponse> findById(
            @PathVariable("book-id") Integer bookId) {
        return ResponseEntity.ok(bookService.findById(bookId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.findAllBooks(page, size, authentication));
    }

    @GetMapping("/owner")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.findAllBooksByOwner(page, size, authentication));
    }

    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllBorrowedBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.findAllBorrowedBooks(page, size, authentication));
    }

    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllReturnedBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.findAllReturnedBooks(page, size, authentication));
    }

    @PatchMapping("shareable/{book-id}")
    public ResponseEntity<Integer> updateShareableStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.updateShareableStatus(bookId, authentication));
    }

    @PatchMapping("archived/{book-id}")
    public ResponseEntity<Integer> updateArchivedStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.updateArchivedStatus(bookId, authentication));
    }

    @PostMapping("borrow/{book-id}")
    public ResponseEntity<Integer> borrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.borrowBook(bookId, authentication));
    }

    @PatchMapping("borrow/return/{book-id}")
    public ResponseEntity<Integer> returnBorrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.returnBorrowBook(bookId, authentication));
    }

    @PatchMapping("borrow/return/approve/{book-id}")
    public ResponseEntity<Integer> approveReturnBorrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication authentication) {
        return ResponseEntity.ok(bookService.approveReturnBorrowBook(bookId, authentication));
    }

    @PostMapping(value = "/cover/{book-id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadBookCoverPicture(
            @PathVariable("book-id") Integer bookId,
            @Parameter
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        bookService.uploadBookCoverPicture(file, authentication, bookId);
        return ResponseEntity.accepted().build();
    }

}
