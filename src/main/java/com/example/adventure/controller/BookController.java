package com.example.adventure.controller;

import com.example.adventure.model.Book;
import com.example.adventure.model.dto.BookSummary;
import com.example.adventure.service.BookService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {
  private final BookService books;

  public BookController(BookService books) {
    this.books = books;
  }

  @GetMapping
  public List<BookSummary> list(
      @RequestParam(defaultValue = "") String search,
      @RequestParam(required = false) Book.Difficulty difficulty) {
    return books.list(search, difficulty);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookSummary add(@RequestBody Book book) {
    return books.add(book);
  }
}
