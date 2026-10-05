package com.example.adventure.repository;

import com.example.adventure.exception.ApiException;
import com.example.adventure.model.GameState;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class FileGameRepository implements GameRepository {
  private final Path directory;
  private final JsonMapper mapper = JsonMapper.builder().build();

  public FileGameRepository(String directory) throws IOException {
    this.directory = Path.of(directory, "games");
    Files.createDirectories(this.directory);
  }

  @Override
  public GameState find(UUID id) {
    var file = directory.resolve(id + ".json");
    if (!Files.exists(file))
      throw new ApiException(HttpStatus.NOT_FOUND, "Saved adventure not found.");
    try {
      return mapper.readValue(file.toFile(), GameState.class);
    } catch (JacksonException ex) {
      throw new ApiException(
          HttpStatus.INTERNAL_SERVER_ERROR, "The adventure could not be loaded.");
    }
  }

  @Override
  public void save(GameState game) {
    Path temporary = null;
    try {
      temporary = Files.createTempFile(directory, "game-", ".tmp");
      mapper.writeValue(temporary.toFile(), game);
      try {
        Files.move(
            temporary,
            directory.resolve(game.id() + ".json"),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException ex) {
        Files.move(
            temporary, directory.resolve(game.id() + ".json"), StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (JacksonException | IOException ex) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "The adventure could not be saved.");
    } finally {
      if (temporary != null)
        try {
          Files.deleteIfExists(temporary);
        } catch (IOException ignored) {
          /* A stale temporary file is safe to remove later. */
        }
    }
  }
}
