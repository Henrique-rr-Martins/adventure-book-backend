package com.example.adventure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendPathsTest {
  @TempDir Path directory;

  @Test
  void compiledClassesUseTheBackendModuleDirectory() {
    assertEquals(directory, BackendPaths.moduleDirectory(directory.resolve("target/classes")));
  }

  @Test
  void mavenJarUsesTheBackendModuleDirectory() {
    assertEquals(directory, BackendPaths.moduleDirectory(directory.resolve("target/app.jar")));
  }

  @Test
  void deployedJarUsesItsOwnDirectory() {
    assertEquals(directory, BackendPaths.moduleDirectory(directory.resolve("app.jar")));
  }

  @Test
  void absoluteConfigurationIsPreserved() throws Exception {
    assertEquals(directory.toString(), BackendPaths.resolve(directory.toString()));
  }
}
