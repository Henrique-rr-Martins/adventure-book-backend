package com.example.adventure.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.adventure.exception.ApiException;
import com.example.adventure.repository.FileBookRepository;
import com.example.adventure.repository.FileGameRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameServiceTest {
  @TempDir Path data;

  private GameService service() throws Exception {
    return new GameService(
        new FileBookRepository(new BookValidator(), "./data/books", data.toString()),
        new FileGameRepository(data.toString()),
        new GameEngine());
  }

  @Test
  void progressSurvivesRestartAndStaleChoicesAreRejected() throws Exception {
    var service = service();
    var start = service.start("forest-key");
    var next = service.choose(start.id(), 0, start.revision());
    var restored = service().get(start.id());
    assertEquals(next, restored);
    var error =
        assertThrows(ApiException.class, () -> service.choose(start.id(), 0, start.revision()));
    assertEquals(409, error.status().value());
    assertEquals(next, service.get(start.id()));
  }

  @Test
  void rejectsInvalidBooksBeforeCreatingAGame() throws Exception {
    Files.createDirectories(data.resolve("books"));
    Files.writeString(data.resolve("books/invalid-book.json"), "not json");
    var error = assertThrows(ApiException.class, () -> service().start("invalid-book"));
    assertEquals(422, error.status().value());
  }
}
