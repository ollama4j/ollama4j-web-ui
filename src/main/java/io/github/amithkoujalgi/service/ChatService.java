package io.github.amithkoujalgi.service;

import io.github.amithkoujalgi.data.ModelItem;
import io.github.amithkoujalgi.data.ModelListItem;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.OllamaStreamHandler;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.Model;
import io.github.amithkoujalgi.ollama4j.core.models.chat.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatService implements Serializable {

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

  public Collection<ModelListItem> getModels()
      throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
    Collection<Model> models = ollamaAPI.listModels();
    Collection<ModelListItem> modelListItems = new ArrayList<>();
    models.forEach(
        m -> {
          DateTimeFormatter formatter =
              DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm a").withZone(ZoneId.systemDefault());
          ZonedDateTime dateTime =
              ZonedDateTime.parse(m.getModifiedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
          modelListItems.add(
              new ModelListItem(
                  m.getModelName(),
                  m.getModel(),
                  dateTime.format(formatter),
                  m.getDigest(),
                  FileUtils.byteCountToDisplaySize(m.getSize())));
        });
    return modelListItems;
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
