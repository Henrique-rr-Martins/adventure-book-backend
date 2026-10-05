package com.example.adventure.service;

import com.example.adventure.model.Book;
import com.example.adventure.model.BookEntry;
import com.example.adventure.model.dto.BookSummary;
import com.example.adventure.repository.BookRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class BookService {
  private final BookRepository books;

  public BookService(BookRepository books) {
    this.books = books;
  }

  public List<BookSummary> list(String search, Book.Difficulty difficulty) {
    String term = search.strip().toLowerCase(Locale.ROOT);
    return books.findAll().stream()
        .map(this::summary)
        .filter(
            book -> (book.title() + " " + book.author()).toLowerCase(Locale.ROOT).contains(term))
        .filter(book -> difficulty == null || book.difficulty() == difficulty)
        .toList();
  }

  public BookSummary add(Book book) {
    return summary(books.add(book));
  }

  private BookSummary summary(BookEntry entry) {
    Book book = entry.book();
    return new BookSummary(
        entry.id(),
        book == null || book.title() == null || book.title().isBlank() ? entry.id() : book.title(),
        book == null || book.author() == null || book.author().isBlank()
            ? "Unknown"
            : book.author(),
        book == null ? null : book.difficulty(),
        entry.valid(),
        entry.errors());
  }
}
