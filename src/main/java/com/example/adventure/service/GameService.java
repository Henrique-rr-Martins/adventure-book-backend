package com.example.adventure.service;

import com.example.adventure.exception.ApiException;
import com.example.adventure.model.Book;
import com.example.adventure.model.GameState;
import com.example.adventure.model.dto.GameView;
import com.example.adventure.repository.BookRepository;
import com.example.adventure.repository.GameRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GameService {
  private final BookRepository books;
  private final GameRepository games;
  private final GameEngine engine;

  public GameService(BookRepository books, GameRepository games, GameEngine engine) {
    this.books = books;
    this.games = games;
    this.engine = engine;
  }

  public synchronized GameView start(String bookId) {
    Book book = playableBook(bookId);
    var game = engine.start(bookId, book);
    games.save(game);
    return view(game, book, null);
  }

  public synchronized GameView get(UUID id) {
    var game = games.find(id);
    return view(game, playableBook(game.bookId()), null);
  }

  public synchronized GameView choose(UUID id, int index, long expectedRevision) {
    var game = games.find(id);
    if (game.revision() != expectedRevision)
      throw new ApiException(
          HttpStatus.CONFLICT, "The adventure has changed. Reload it before choosing again.");
    Book book = playableBook(game.bookId());
    var updated = engine.choose(game, book, index);
    var consequence = engine.section(book, game.sectionId()).choices().get(index).consequence();
    games.save(updated);
    return view(updated, book, consequence == null ? null : consequence.text());
  }

  private Book playableBook(String id) {
    var entry = books.find(id);
    if (!entry.valid())
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_CONTENT,
          "This book is invalid: " + String.join(" ", entry.errors()));
    return entry.book();
  }

  private GameView view(GameState game, Book book, String consequence) {
    var section = engine.section(book, game.sectionId());
    return new GameView(
        game.id(),
        game.bookId(),
        book.title() == null || book.title().isBlank() ? game.bookId() : book.title(),
        game.sectionId(),
        game.health(),
        game.status(),
        section.text(),
        game.status() == GameState.Status.PLAYING
            ? section.choices().stream().map(Book.Option::description).toList()
            : List.of(),
        consequence,
        game.revision());
  }
}
