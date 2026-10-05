package com.example.adventure.repository;

import com.example.adventure.model.GameState;
import java.util.UUID;

public interface GameRepository {
  GameState find(UUID id);

  void save(GameState game);
}
