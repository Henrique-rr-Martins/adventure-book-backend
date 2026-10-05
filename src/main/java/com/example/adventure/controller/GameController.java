package com.example.adventure.controller;

import com.example.adventure.exception.ApiException;
import com.example.adventure.model.dto.ChoiceRequest;
import com.example.adventure.model.dto.GameView;
import com.example.adventure.model.dto.StartGameRequest;
import com.example.adventure.service.GameService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {
  private final GameService games;

  public GameController(GameService games) {
    this.games = games;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GameView start(@RequestBody StartGameRequest request) {
    if (request.bookId() == null || request.bookId().isBlank())
      throw new ApiException(HttpStatus.BAD_REQUEST, "A book is required.");
    return games.start(request.bookId());
  }

  @GetMapping("/{id}")
  public GameView get(@PathVariable UUID id) {
    return games.get(id);
  }

  @PostMapping("/{id}/choices")
  public GameView choose(@PathVariable UUID id, @RequestBody ChoiceRequest request) {
    if (request.index() == null || request.revision() == null)
      throw new ApiException(HttpStatus.BAD_REQUEST, "A choice and current revision are required.");
    return games.choose(id, request.index(), request.revision());
  }
}
