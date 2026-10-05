package com.example.adventure.model;

import java.util.List;

public record BookEntry(String id, Book book, List<String> errors) {
  public boolean valid() {
    return errors.isEmpty();
  }
}
