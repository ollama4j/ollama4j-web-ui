#!/bin/bash

mvn -Drevision=0.0.2 clean package
#mvn clean package -Pproduction
java -jar target/ollama4j-web-ui-0.0.1.jar