# Ollama4j Web UI

A web UI for [Ollama](https://ollama.com/) written in Java
using [Spring Boot](https://spring.io/projects/spring-boot/) and [Vaadin](https://vaadin.com/) framework
and [Ollama4j](https://github.com/amithkoujalgi/ollama4j).

The goal of the project is to enable Ollama users coming from Java and Spring background to have a fully functional web
UI.

This project focuses on the raw capabilities of interacting with various models running on Ollama servers.

## Table of Contents

- [How does it work?](#how-does-it-work)
- [Requirements](#requirements)
- [Run the app](#running-the-application)
- [Screenshots](#screenshots)
- [Dev Contributions](#get-involved)

#### How does it work?

```mermaid
  flowchart LR
    owui[Ollama4j Web UI]
    o4j[Ollama4j]
    o[Ollama Server]
    owui -->|uses| o4j
    o4j -->|Communicates with| o;
    m[Models]
    subgraph Ollama Deployment
        direction TB
        o -->|Manages| m
    end
```

#### Requirements

![Java](https://img.shields.io/badge/Java-17_+-blue.svg?style=just-the-message&labelColor=gray)

## Running the application

### Download

Download the latest version from [here](https://github.com/ollama4j/ollama4j-web-ui/releases).

Or, you could download it via command-line.
Just make sure to specify the version you want to download.

```shell
VERSION=0.0.1; wget https://github.com/ollama4j/ollama4j-web-ui/releases/download/$VERSION/ollama4j-web-ui-$VERSION.jar
```

### Configure

Create a file `application.properties` and add the following configuration.
Update the values of `server.port` and `ollama.url` according to your needs.

```shell
server.port=8080
logging.level.org.atmosphere = warn

spring.mustache.check-template-location = false
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

vaadin.launch-browser=true
vaadin.whitelisted-packages = com.vaadin,org.vaadin,dev.hilla,io.github.amithkoujalgi

ollama.url=http://localhost:11434
ollama.request-timeout-seconds=120
```

### Run the app

```shell
java -jar ollama4j-web-ui-$VERSION.jar \
  --spring.config.location=/path/to/your/application.properties
```

Then open http://localhost:8080 in your browser to access the Ollama4j Web UI.

## Screenshots

<img width="2048" alt="Screenshot 2024-03-02 at 12 17 32 AM" src="https://github.com/ollama4j/ollama4j-web-ui/assets/1876165/426c7ab7-2a27-4bae-87db-daa6a499395c">
<img width="2048" alt="Screenshot 2024-03-02 at 12 23 47 AM" src="https://github.com/ollama4j/ollama4j-web-ui/assets/1876165/c697fd1d-0323-4351-b9a7-25166eff4c7e">
<img width="2048" alt="Screenshot 2024-03-02 at 12 23 34 AM" src="https://github.com/ollama4j/ollama4j-web-ui/assets/1876165/1ac899bc-a19e-4dc0-bee1-63b67e8fb346">

### Get Involved

Contributions are most welcome! Whether it's reporting a bug, proposing an enhancement, or helping
with code - any sort
of contribution is much appreciated.

### Credits

The project is inspired by the awesome [ollama4j-ui](https://github.com/AgentSchmecker/ollama4j-ui) project
by [@AgentSchmecker](https://github.com/AgentSchmecker).

The nomenclature has been adopted from the incredible [Ollama](https://ollama.ai/)
project.
