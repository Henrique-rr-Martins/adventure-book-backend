package com.example.adventure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adventure")
public record AdventureProperties(String booksDirectory, String dataDirectory) {}
