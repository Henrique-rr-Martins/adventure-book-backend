package com.example.adventure.service;

import static com.example.adventure.model.GameState.Status.*;

import com.example.adventure.exception.ApiException;
import com.example.adventure.model.Book;
import com.example.adventure.model.GameState;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GameEngine {
  public static final int INITIAL_HEALTH = 10;

  public GameState start(String bookId, Book book) {
    var beginning =
        book.sections().stream()
            .filter(s -> s.type() == Book.SectionType.BEGIN)
            .findFirst()
            .orElseThrow();
    return new GameState(UUID.randomUUID(), bookId, beginning.id(), INITIAL_HEALTH, PLAYING, 0);
  }

  public GameState choose(GameState game, Book book, int choiceIndex) {
    if (game.status() != PLAYING)
      throw new ApiException(HttpStatus.CONFLICT, "This adventure has ended.");
    var section = section(book, game.sectionId());
    if (choiceIndex < 0 || choiceIndex >= section.choices().size())
      throw new ApiException(HttpStatus.BAD_REQUEST, "Choose an available option.");
    var option = section.choices().get(choiceIndex);
    long health = game.health();
    var consequence = option.consequence();
    if (consequence != null) {
      int change =
          consequence.type() == Book.ConsequenceType.LOSE_HEALTH
              ? -consequence.value()
              : consequence.value();
      health = Math.max(health + change, 0L);
    }
    var next = section(book, option.gotoId());
    var status = health == 0 ? LOST : next.type() == Book.SectionType.END ? WON : PLAYING;
    return new GameState(game.id(), game.bookId(), next.id(), health, status, game.revision() + 1);
  }

  public Book.Section section(Book book, String id) {
    return book.sections().stream().filter(s -> s.id().equals(id)).findFirst().orElseThrow();
  }
}
