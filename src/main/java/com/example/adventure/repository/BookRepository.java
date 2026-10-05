package com.example.adventure.repository;

import com.example.adventure.model.Book;
import com.example.adventure.model.BookEntry;
import java.util.List;

public interface BookRepository {
  List<BookEntry> findAll();

  BookEntry find(String id);

  BookEntry add(Book book);
}
