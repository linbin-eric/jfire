# Jfire Project Context

## Project Overview

This is a Java project named **Jfire**. Based on the `pom.xml` file, Jfire is an Inversion of Control (IOC) and Aspect-Oriented Programming (AOP) container. Its primary purpose is to provide transparent object management, a simple and efficient AOP API, and support for transparent transaction management. The project is licensed under the GNU Affero General Public License v3.0.

*   **Main Technologies**: Java (JDK 17), Maven, Log4j2, Lombok, JUnit.
*   **Core Dependencies**: `com.jfirer:baseutil`, `com.jfirer:jfireEL`.
*   **Build Tool**: Apache Maven.

## Building and Running

This is a standard Maven project. Here are the typical commands used for building, testing, and packaging:

*   **Clean Build**: `mvn clean install`
    *   This command cleans the project, compiles the source code, runs tests, and packages the application.
*   **Compile**: `mvn compile`
    *   Compiles the main source code.
*   **Test**: `mvn test`
    *   Compiles and runs the unit tests.
*   **Package**: `mvn package`
    *   Packages the compiled code into a JAR file (named `jfire.jar` as per `pom.xml`).

The project also configures the `maven-source-plugin` to generate a source JAR during the `compile` phase.

## Development Conventions

*   **Java Version**: The project targets Java 17.
*   **Testing**: JUnit 4 is used for testing. Test resources (like `config.json`, `log4j2.xml`) are located in `src/test/resources`.
*   **Logging**: Log4j2 is used for logging. Configuration is defined in `src/test/resources/log4j2.xml`.
*   **Code Style**: Lombok is used to reduce boilerplate code.
*   **Project Structure**:
    *   `src/main/java`: Contains the main source code.
    *   `src/test/java`: Contains unit tests.
    *   `src/test/resources`: Contains test configuration files.