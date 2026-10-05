package com.example.adventure.service;

import static com.example.adventure.model.Book.*;
import static com.example.adventure.model.Book.SectionType.*;
import static com.example.adventure.model.GameState.Status.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.adventure.exception.ApiException;
import com.example.adventure.model.Book;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameEngineTest {
  private final GameEngine engine = new GameEngine();

  private Book book(ConsequenceType type, int value) {
    return new Book(
        "Test",
        "Author",
        Difficulty.EASY,
        List.of(
            new Section(
                "1",
                "Beginning",
                BEGIN,
                List.of(new Option("Continue", "2", new Consequence(type, value, "Result")))),
            new Section("2", "Ending", END, null)));
  }

  @Test
  void startsWithTenHealthAndWinsAtAnEnding() {
    var book = book(ConsequenceType.LOSE_HEALTH, 3);
    var start = engine.start("test", book);
    assertEquals(10, start.health());
    assertEquals(PLAYING, start.status());
    var end = engine.choose(start, book, 0);
    assertEquals(7, end.health());
    assertEquals(WON, end.status());
    assertEquals(1, end.revision());
  }

  @Test
  void deathTakesPriorityOverAnEndingAndHealthCannotBeNegative() {
    var book = book(ConsequenceType.LOSE_HEALTH, 20);
    var end = engine.choose(engine.start("test", book), book, 0);
    assertEquals(0, end.health());
    assertEquals(LOST, end.status());
  }

  @Test
  void healingCanExceedStartingHealth() {
    var book = book(ConsequenceType.GAIN_HEALTH, 5);
    assertEquals(15, engine.choose(engine.start("test", book), book, 0).health());
  }

  @Test
  void rejectsUnavailableChoicesAndChoicesAfterTheGameEnds() {
    var book = book(ConsequenceType.LOSE_HEALTH, 3);
    var start = engine.start("test", book);
    assertThrows(ApiException.class, () -> engine.choose(start, book, -1));
    assertThrows(ApiException.class, () -> engine.choose(start, book, 1));
    var end = engine.choose(start, book, 0);
    assertThrows(ApiException.class, () -> engine.choose(end, book, 0));
  }

  @Test
  void largeHealingDoesNotOverflowIntegerHealth() {
    var book = book(ConsequenceType.GAIN_HEALTH, Integer.MAX_VALUE);
    assertEquals(
        10L + Integer.MAX_VALUE, engine.choose(engine.start("test", book), book, 0).health());
  }

  @Test
  void zeroConsequenceKeepsCurrentHealth() {
    var book = book(ConsequenceType.LOSE_HEALTH, 0);
    assertEquals(10, engine.choose(engine.start("test", book), book, 0).health());
  }
}
