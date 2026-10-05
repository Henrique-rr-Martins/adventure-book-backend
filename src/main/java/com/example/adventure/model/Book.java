package com.example.adventure.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Book(String title, String author, Difficulty difficulty, List<Section> sections) {
  public enum Difficulty {
    EASY,
    MEDIUM,
    HARD
  }

  public enum SectionType {
    BEGIN,
    NODE,
    END
  }

  public enum ConsequenceType {
    LOSE_HEALTH,
    GAIN_HEALTH
  }

  public record Section(String id, String text, SectionType type, List<Option> options) {
    public List<Option> choices() {
      return options == null ? List.of() : options;
    }
  }

  public record Option(String description, String gotoId, Consequence consequence) {}

  public record Consequence(ConsequenceType type, Integer value, String text) {}
}
