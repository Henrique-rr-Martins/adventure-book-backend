package com.example.adventure.model;

import java.util.UUID;

public record GameState(
    UUID id, String bookId, String sectionId, long health, Status status, long revision) {
  public enum Status {
    PLAYING,
    WON,
    LOST
  }
}
