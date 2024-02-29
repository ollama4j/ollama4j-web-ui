package io.github.amithkoujalgi.config;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
  @Value("${ollama.url}")
  private String ollamaUrl;

  @Value("${ollama.request-timeout-seconds:120}")
  private int ollamaRequestTimeoutSeconds;

  @Bean
  OllamaAPI getOllamaAPI() {
    OllamaAPI ollamaAPI = new OllamaAPI(ollamaUrl);
    ollamaAPI.setRequestTimeoutSeconds(ollamaRequestTimeoutSeconds);
    return ollamaAPI;
  }
}
