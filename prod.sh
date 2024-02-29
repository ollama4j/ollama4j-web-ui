#!/bin/bash

mvn clean package -Pproduction
java -jar target/ollama4j-web-ui-1.0-SNAPSHOT.jar