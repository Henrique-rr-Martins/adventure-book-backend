package com.example.adventure.model.dto;

import com.example.adventure.model.GameState;
import java.util.List;
import java.util.UUID;

public record GameView(
    UUID id,
    String bookId,
    String title,
    String sectionId,
    long health,
    GameState.Status status,
    String text,
    List<String> choices,
    String consequence,
    long revision) {}
