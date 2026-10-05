# Adventure Book — backend



## Requirements


JDK: 21

Maven: 3.9


## Dependencies

| Dependency or build tool | Version | Purpose |
| --- | --- | --- |
| `spring-boot-starter-parent` | 4.0.8 | Dependency versions and Maven build defaults. |
| `spring-boot-starter-webmvc` | Managed by Spring Boot 4.0.8 | REST controllers, embedded HTTP server, and JSON conversion. |
| `spring-boot-starter-test` | Managed by Spring Boot 4.0.8 | Backend tests with JUnit and Spring testing support; test scope only. |
| `spring-boot-maven-plugin` | Managed by Spring Boot 4.0.8 | Local startup and executable JAR packaging. |


## Start the application



Run this command in the project folder:



```text

mvn spring-boot:run

```



The backend runs at http://127.0.0.1:8080.



## Books and saved data



All required books are included in `data/books/`, and saved games are created in `data/games/`. The backend does not depend on any directory outside `backend/`. To use another data folder, override both `adventure.books-directory` and `adventure.data-directory`; include the book files in that location.


## Build and test



```text

mvn verify

```



`mvn verify` runs the Java tests and produces `target/adventure-backend-1.0.0.jar`. From the project folder, the packaged backend can be started with:



```text

java -jar target/adventure-backend-1.0.0.jar

```

Relative data paths are resolved from the backend application location, so starting it from the repository root in an IDE also loads `data/books/`.
