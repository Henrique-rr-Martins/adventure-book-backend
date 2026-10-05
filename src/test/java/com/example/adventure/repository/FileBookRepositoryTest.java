package com.example.adventure.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.adventure.service.BookValidator;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileBookRepositoryTest {
  @TempDir Path data;

  @Test
  void malformedBooksAreListedWithoutChangingTheirFiles() throws Exception {
    var directory = Files.createDirectories(data.resolve("books"));
    var file = directory.resolve("malformed.json");
    Files.writeString(file, "not json");
    var repository =
        new FileBookRepository(new BookValidator(), directory.toString(), data.toString());
    assertEquals(1, repository.findAll().size());
    assertFalse(repository.find("malformed").valid());
    assertEquals("not json", Files.readString(file));
  }

  @Test
  void importedBooksSurviveARepositoryRestart() throws Exception {
    var first = new FileBookRepository(new BookValidator(), "./data/books", data.toString());
    var imported = first.add(first.find("forest-key").book());
    var second = new FileBookRepository(new BookValidator(), "./data/books", data.toString());
    assertTrue(second.find(imported.id()).valid());
    assertEquals("The Forest Key", second.find(imported.id()).book().title());
  }
}
