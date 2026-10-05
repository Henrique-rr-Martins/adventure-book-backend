package com.example.adventure.repository;

import com.example.adventure.exception.ApiException;
import com.example.adventure.model.Book;
import com.example.adventure.model.BookEntry;
import com.example.adventure.service.BookValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class FileBookRepository implements BookRepository {
  private final Map<String, BookEntry> books = new ConcurrentHashMap<>();
  private final JsonMapper mapper = JsonMapper.builder().build();
  private final BookValidator validator;
  private final Path uploads;

  public FileBookRepository(BookValidator validator, String directory, String dataDirectory)
      throws IOException {
    this.validator = validator;
    this.uploads = Path.of(dataDirectory, "books");
    Files.createDirectories(uploads);
    Path suppliedBooks = Path.of(directory).toAbsolutePath().normalize();
    load(suppliedBooks);
    if (!suppliedBooks.equals(uploads.toAbsolutePath().normalize())) {
      load(uploads);
    }
  }

  private void load(Path directory) throws IOException {
    if (!Files.isDirectory(directory))
      throw new IOException("Book directory does not exist: " + directory);
    try (var paths = Files.list(directory)) {
      for (var path :
          paths.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
        String id = path.getFileName().toString().replaceFirst("\\.json$", "");
        try {
          Book book = mapper.readValue(path.toFile(), Book.class);
          books.put(id, new BookEntry(id, book, validator.validate(book)));
        } catch (JacksonException | IllegalArgumentException ex) {
          books.put(
              id,
              new BookEntry(
                  id, null, List.of("The file is empty or is not a supported book JSON.")));
        }
      }
    }
  }

  @Override
  public List<BookEntry> findAll() {
    return books.values().stream().sorted(Comparator.comparing(BookEntry::id)).toList();
  }

  @Override
  public BookEntry find(String id) {
    var entry = books.get(id);
    if (entry == null) throw new ApiException(HttpStatus.NOT_FOUND, "Book not found.");
    return entry;
  }

  @Override
  public synchronized BookEntry add(Book book) {
    var errors = validator.validate(book);
    if (!errors.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, String.join(" ", errors));
    String id = UUID.randomUUID().toString();
    try {
      mapper.writeValue(uploads.resolve(id + ".json").toFile(), book);
    } catch (JacksonException ex) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "The book could not be saved.");
    }
    var entry = new BookEntry(id, book, List.of());
    books.put(id, entry);
    return entry;
  }
}
