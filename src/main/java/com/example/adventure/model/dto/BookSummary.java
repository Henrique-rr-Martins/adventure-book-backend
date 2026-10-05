package com.example.adventure.model.dto;

import com.example.adventure.model.Book;
import java.util.List;

public record BookSummary(
    String id,
    String title,
    String author,
    Book.Difficulty difficulty,
    boolean valid,
    List<String> errors) {}
