package io.github.amithkoujalgi.service;

import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.OllamaStreamHandler;
import io.github.amithkoujalgi.ollama4j.core.models.chat.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatService implements Serializable {
  @Value("${ollama.url}")
  private String ollamaUrl;

  @Value("${ollama.model}")
  private String ollamaModel;

  @Value("${ollama.request-timeout-seconds:120}")
  private int ollamaRequestTimeoutSeconds;

  private List<OllamaChatMessage> messages = new ArrayList<>();

  public String getOllamaModel() {
    return ollamaModel;
  }

  public void ask(String message, OllamaStreamHandler streamHandler) {
    OllamaAPI ollamaAPI = new OllamaAPI(ollamaUrl);
    ollamaAPI.setRequestTimeoutSeconds(ollamaRequestTimeoutSeconds);
    OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(ollamaModel);
    OllamaChatRequestModel ollamaChatRequestModel =
        builder.withMessages(messages).withMessage(OllamaChatMessageRole.USER, message).build();
    try {
      OllamaChatResult chat = ollamaAPI.chat(ollamaChatRequestModel, streamHandler);
      messages = chat.getChatHistory();
      chat.getResponse();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
