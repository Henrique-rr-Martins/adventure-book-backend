package com.example.adventure.service;

import static com.example.adventure.model.Book.SectionType.*;

import com.example.adventure.model.Book;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BookValidator {
  public List<String> validate(Book book) {
    var errors = new ArrayList<String>();
    if (book == null) return List.of("The book is empty.");
    if (book.sections() == null || book.sections().isEmpty()) {
      errors.add("The book must have sections.");
      return List.copyOf(errors);
    }
    if (book.sections().stream().filter(s -> s != null && s.type() == BEGIN).count() != 1)
      errors.add("The book must have exactly one beginning.");
    if (book.sections().stream().noneMatch(s -> s != null && s.type() == END))
      errors.add("The book must have at least one ending.");
    var ids = new HashSet<String>();
    for (var section : book.sections()) {
      if (section == null) {
        errors.add("A section cannot be null.");
        continue;
      }
      if (blank(section.id()) || !ids.add(section.id()))
        errors.add("Section IDs must be present and unique.");
      if (blank(section.text()) || section.type() == null)
        errors.add("Section " + section.id() + " needs text and a type.");
      if (section.type() != END && section.choices().isEmpty())
        errors.add("Section " + section.id() + " has no choices.");
    }
    for (var section : book.sections()) {
      if (section == null) continue;
      for (var option : section.choices()) {
        if (option == null) {
          errors.add("A choice cannot be null.");
          continue;
        }
        if (blank(option.description())) errors.add("A choice needs a description.");
        if (!ids.contains(option.gotoId()))
          errors.add(
              "Section " + section.id() + " points to missing section " + option.gotoId() + ".");
        var consequence = option.consequence();
        if (consequence != null
            && (consequence.type() == null
                || consequence.value() == null
                || consequence.value() < 0))
          errors.add("Consequences need a supported type and a nonnegative value.");
      }
    }
    return List.copyOf(errors);
  }

  private boolean blank(String value) {
    return value == null || value.isBlank();
  }
}
