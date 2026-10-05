package com.example.adventure.configuration;

import com.example.adventure.AdventureApplication;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.springframework.boot.system.ApplicationHome;

final class BackendPaths {
  private BackendPaths() {}

  static String resolve(String configuredPath) throws IOException {
    Path path = Path.of(configuredPath);
    if (path.isAbsolute()) {
      return path.normalize().toString();
    }
    try {
      var location =
          AdventureApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI();
      if ("file".equals(location.getScheme())) {
        return moduleDirectory(Path.of(location)).resolve(path).normalize().toString();
      }
      var source = new ApplicationHome(AdventureApplication.class).getSource();
      if (source == null) {
        throw new IOException("Cannot locate the backend directory.");
      }
      return moduleDirectory(source.toPath()).resolve(path).normalize().toString();
    } catch (URISyntaxException exception) {
      throw new IOException("Cannot locate the backend directory.", exception);
    }
  }

  static Path moduleDirectory(Path application) {
    Path directory =
        application.toString().endsWith(".jar") ? application.getParent() : application;
    if (directory.endsWith(Path.of("target", "classes"))) {
      return directory.getParent().getParent();
    }
    if (directory.endsWith("target")) {
      return directory.getParent();
    }
    return directory;
  }
}
