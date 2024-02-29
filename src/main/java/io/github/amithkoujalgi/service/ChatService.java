package io.github.amithkoujalgi.service;

import io.github.amithkoujalgi.data.ModelItem;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.OllamaStreamHandler;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.Model;
import io.github.amithkoujalgi.ollama4j.core.models.chat.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatService implements Serializable {

  @Getter
  @Value("${ollama.model}")
  private String ollamaModel;

  @Autowired private OllamaAPI ollamaAPI;
  private List<OllamaChatMessage> messages = new ArrayList<>();

  public void clearMessages() {
    messages.clear();
  }

  public Collection<ModelItem> getModelItems()
      throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
    Collection<ModelItem> modelItems = new ArrayList<>(Collections.emptyList());
    ollamaAPI
        .listModels()
        .forEach(x -> modelItems.add(new ModelItem(x.getModelName(), x.getModelVersion())));
    return modelItems;
  }

  public Collection<ModelItem> getImageModelItems()
      throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
    Collection<ModelItem> modelItems = new ArrayList<>(Collections.emptyList());
    modelItems.add(new ModelItem("llava", "latest"));
    return modelItems;
  }

  public Collection<Model> getModels()
      throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
    return ollamaAPI.listModels();
  }

  public void ask(String message, String model, OllamaStreamHandler streamHandler) {
    OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(model);
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

  public void askWithImages(
      String message, List<File> imageFiles, String model, OllamaStreamHandler streamHandler) {
    OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(model);
    OllamaChatRequestModel ollamaChatRequestModel =
        builder
            .withMessages(messages)
            .withMessage(OllamaChatMessageRole.USER, message, imageFiles)
            .build();
    try {
      OllamaChatResult chat = ollamaAPI.chat(ollamaChatRequestModel, streamHandler);
      messages = chat.getChatHistory();
      chat.getResponse();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
