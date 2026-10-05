package com.example.adventure.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.example.adventure.model.dto.BookSummary;
import com.example.adventure.model.dto.GameView;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdventureApiTest {
  @TempDir static Path data;

  @Value("${local.server.port}")
  int port;

  private final HttpClient client = HttpClient.newHttpClient();
  private final JsonMapper mapper = JsonMapper.builder().build();

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("adventure.data-directory", () -> data.toString());
  }

  private HttpResponse<String> request(String method, String path, String body) throws Exception {
    var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
    if (body == null) builder.GET();
    else
      builder
          .header("Content-Type", "application/json")
          .method(method, HttpRequest.BodyPublishers.ofString(body));
    return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void listsAndFiltersTheDemoBook() throws Exception {
    var response = request("GET", "/api/books", null);
    assertEquals(200, response.statusCode());
    var books = mapper.readValue(response.body(), BookSummary[].class);
    assertTrue(books.length >= 1);
    assertEquals(
        1,
        java.util.Arrays.stream(books)
            .filter(book -> book.id().equals("forest-key") && book.valid())
            .count());
    var filtered = request("GET", "/api/books?search=forest&difficulty=EASY", null);
    assertEquals(1, mapper.readValue(filtered.body(), BookSummary[].class).length);
  }

  @Test
  void completesAndRestoresAGameAndRejectsDuplicateChoices() throws Exception {
    var started = request("POST", "/api/games", "{\"bookId\":\"forest-key\"}");
    assertEquals(201, started.statusCode());
    var game = mapper.readValue(started.body(), GameView.class);
    String route = "/api/games/" + game.id();
    assertEquals(10, game.health());
    var chosen = request("POST", route + "/choices", "{\"index\":0,\"revision\":0}");
    assertEquals(200, chosen.statusCode());
    assertEquals("2", mapper.readValue(chosen.body(), GameView.class).sectionId());
    assertEquals(
        409, request("POST", route + "/choices", "{\"index\":0,\"revision\":0}").statusCode());
    var finished = request("POST", route + "/choices", "{\"index\":0,\"revision\":1}");
    var ending = mapper.readValue(finished.body(), GameView.class);
    assertEquals(7, ending.health());
    assertEquals("WON", ending.status().name());
    assertTrue(ending.choices().isEmpty());
    var restored = mapper.readValue(request("GET", route, null).body(), GameView.class);
    assertEquals(ending.health(), restored.health());
    assertEquals(ending.revision(), restored.revision());
  }

  @Test
  void rejectsMalformedRequestsAndInvalidImports() throws Exception {
    assertEquals(400, request("POST", "/api/games", "{}").statusCode());
    assertEquals(400, request("POST", "/api/games", "not json").statusCode());
    assertEquals(400, request("POST", "/api/books", "{}").statusCode());
    assertEquals(400, request("GET", "/api/games/not-a-uuid", null).statusCode());
    assertEquals(404, request("POST", "/api/games", "{\"bookId\":\"missing\"}").statusCode());
  }

  @Test
  void importsWithoutDifficultyAndPersistsHealthAboveTen() throws Exception {
    String book =
        """
{"title":"Healing", "sections":[
  {"id":1,"type":"BEGIN","text":"Start","options":[
    {"description":"Heal","gotoId":2,"consequence":{"type":"GAIN_HEALTH","value":150,"text":"Restored"}}]},
  {"id":2,"type":"END","text":"End","options":[{"description":"Unused","gotoId":1}]}]}
""";
    var imported = request("POST", "/api/books", book);
    assertEquals(201, imported.statusCode());
    var summary = mapper.readValue(imported.body(), BookSummary.class);
    assertNull(summary.difficulty());
    assertEquals("Unknown", summary.author());
    assertTrue(summary.valid());
    var started = request("POST", "/api/games", "{\"bookId\":\"" + summary.id() + "\"}");
    assertEquals(201, started.statusCode());
    var game = mapper.readValue(started.body(), GameView.class);
    String route = "/api/games/" + game.id();
    var chosen = request("POST", route + "/choices", "{\"index\":0,\"revision\":0}");
    assertEquals(200, chosen.statusCode());
    var ending = mapper.readValue(chosen.body(), GameView.class);
    assertEquals(160, ending.health());
    assertEquals("WON", ending.status().name());
    assertTrue(ending.choices().isEmpty());
    var restored = mapper.readValue(request("GET", route, null).body(), GameView.class);
    assertEquals(160, restored.health());
  }
}
