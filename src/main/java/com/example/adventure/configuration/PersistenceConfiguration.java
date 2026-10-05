package com.example.adventure.configuration;

import com.example.adventure.repository.BookRepository;
import com.example.adventure.repository.FileBookRepository;
import com.example.adventure.repository.FileGameRepository;
import com.example.adventure.repository.GameRepository;
import com.example.adventure.service.BookValidator;
import java.io.IOException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AdventureProperties.class)
public class PersistenceConfiguration {
  @Bean
  BookRepository bookRepository(BookValidator validator, AdventureProperties properties)
      throws IOException {
    return new FileBookRepository(
        validator,
        BackendPaths.resolve(properties.booksDirectory()),
        BackendPaths.resolve(properties.dataDirectory()));
  }

  @Bean
  GameRepository gameRepository(AdventureProperties properties) throws IOException {
    return new FileGameRepository(BackendPaths.resolve(properties.dataDirectory()));
  }
}
