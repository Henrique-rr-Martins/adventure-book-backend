package com.example.adventure.service;

import static com.example.adventure.model.Book.SectionType.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.adventure.model.Book;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookValidatorTest {
  private final BookValidator validator = new BookValidator();

  private Book book(List<Book.Section> sections) {
    return new Book("Test", "Author", Book.Difficulty.EASY, sections);
  }

  private Book.Section section(String id, Book.SectionType type, String destination) {
    return new Book.Section(
        id,
        "A story",
        type,
        destination == null ? null : List.of(new Book.Option("Continue", destination, null)));
  }

  @Test
  void acceptsAValidBook() {
    assertTrue(
        validator
            .validate(book(List.of(section("1", BEGIN, "2"), section("2", END, null))))
            .isEmpty());
  }

  @Test
  void rejectsMissingAndMultipleBeginnings() {
    assertTrue(
        validator
            .validate(book(List.of(section("2", END, null))))
            .contains("The book must have exactly one beginning."));
    assertTrue(
        validator
            .validate(
                book(
                    List.of(
                        section("1", BEGIN, "3"),
                        section("2", BEGIN, "3"),
                        section("3", END, null))))
            .contains("The book must have exactly one beginning."));
  }

  @Test
  void rejectsMissingEndingsAndMissingDestinations() {
    var errors = validator.validate(book(List.of(section("1", BEGIN, "999"))));
    assertTrue(errors.contains("The book must have at least one ending."));
    assertTrue(errors.stream().anyMatch(e -> e.contains("missing section 999")));
  }

  @Test
  void checksUnreachableSectionsAsWell() {
    var errors =
        validator.validate(
            book(
                List.of(
                    section("1", BEGIN, "2"),
                    section("2", END, null),
                    section("666", NODE, null))));
    assertTrue(errors.contains("Section 666 has no choices."));
  }

  @Test
  void rejectsDuplicateIdsAndInvalidConsequences() {
    var beginning =
        new Book.Section(
            "1",
            "Story",
            BEGIN,
            List.of(
                new Book.Option(
                    "Continue",
                    "1",
                    new Book.Consequence(Book.ConsequenceType.LOSE_HEALTH, -3, "Bad value"))));
    var errors = validator.validate(book(List.of(beginning, section("1", END, null))));
    assertTrue(errors.contains("Section IDs must be present and unique."));
    assertTrue(errors.stream().anyMatch(e -> e.startsWith("Consequences need")));
  }

  @Test
  void handlesNullBooksAndNullSections() {
    assertEquals(List.of("The book is empty."), validator.validate(null));
    var sections = new java.util.ArrayList<Book.Section>();
    sections.add(null);
    assertTrue(validator.validate(book(sections)).contains("A section cannot be null."));
  }

  @Test
  void acceptsOptionalMetadataAndDifficulty() {
    var book =
        new Book(
            "Test", "Author", null, List.of(section("1", BEGIN, "2"), section("2", END, null)));
    assertTrue(validator.validate(book).isEmpty());
  }

  @Test
  void endingMayContainOptionsWithExistingDestinations() {
    assertTrue(
        validator
            .validate(book(List.of(section("1", BEGIN, "2"), section("2", END, "1"))))
            .isEmpty());
  }

  @Test
  void acceptsZeroAndLargeConsequenceValues() {
    for (int value : List.of(0, 150)) {
      var beginning =
          new Book.Section(
              "1",
              "Story",
              BEGIN,
              List.of(
                  new Book.Option(
                      "Continue",
                      "2",
                      new Book.Consequence(Book.ConsequenceType.GAIN_HEALTH, value, "Result"))));
      assertTrue(validator.validate(book(List.of(beginning, section("2", END, null)))).isEmpty());
    }
  }
}
