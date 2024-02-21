package io.github.amithkoujalgi.views.chat;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.OllamaStreamHandler;
import io.github.amithkoujalgi.ollama4j.core.models.chat.*;
import io.github.amithkoujalgi.views.MainLayout;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting
 * message in a notification.
 */
@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ChatView extends VerticalLayout {

  private MessageList chat;
  private MessageInput input;
  private List<MessageListItem> chatEntries = new ArrayList<>();

  public ChatView() {
    H4 header = new H4("Model: LLaMA");

    chat = new MessageList();

    MessageListItem welcome =
        new MessageListItem(
            "Hello there!",
            Instant.now(),
            StringUtils.capitalize(OllamaChatMessageRole.ASSISTANT.name().toLowerCase()));

    input = new MessageInput();
    chat.setItems(welcome);
    add(header, chat, input);
    input.addSubmitListener(this::onSubmit);
    this.setHorizontalComponentAlignment(Alignment.CENTER, chat, input);
    this.setPadding(true); // Leave some white space
    this.setHeightFull(); // We maximize to window
    chat.setSizeFull(); // Chat takes most of the space
    input.setWidthFull(); // Full width only
    chat.setMaxWidth("1200px"); // Limit the width
    input.setMaxWidth("1200px");
  }

  private void onSubmit(MessageInput.SubmitEvent submitEvent) {
    ChatService chatService = new ChatService();
    MessageListItem question =
        new MessageListItem(
            submitEvent.getValue(),
            Instant.now(),
            StringUtils.capitalize(OllamaChatMessageRole.USER.name().toLowerCase()));
    question.setUserAbbreviation("US");
    question.setUserColorIndex(1);
    chatEntries.add(question);
    MessageListItem answer =
        new MessageListItem(
            "Thinking...",
            Instant.now(),
            StringUtils.capitalize(OllamaChatMessageRole.ASSISTANT.name().toLowerCase()));
    chatEntries.add(answer);
    answer.setUserAbbreviation("AS");
    answer.setUserColorIndex(2);
    chat.setItems(chatEntries);
    Thread t =
        new Thread(
            () ->
                chatService.ask(
                    submitEvent.getValue(),
                    (s) -> getUI().ifPresent(ui -> ui.access(() -> answer.setText(s)))));
    t.start();
  }
}

class ChatService implements Serializable {

  private List<OllamaChatMessage> messages = new ArrayList<>();

  public void ask(String message, OllamaStreamHandler streamHandler) {
    OllamaService ollamaService = new OllamaService();
    ollamaService.setModel("llama2");
    ollamaService.setUrl("http://localhost:11434");
    OllamaAPI api = ollamaService.getOllamaAPIInstance();
    api.setRequestTimeoutSeconds(240);
    OllamaChatRequestBuilder builder =
        OllamaChatRequestBuilder.getInstance(ollamaService.getModel());
    OllamaChatRequestModel ollamaChatRequestModel =
        builder.withMessages(messages).withMessage(OllamaChatMessageRole.USER, message).build();
    try {
      OllamaChatResult chat = api.chat(ollamaChatRequestModel, streamHandler);
      messages = chat.getChatHistory();
      chat.getResponse();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

class OllamaService implements Serializable {

  private String url;

  private String model;

  private Integer requestTimeout = 120;

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getRequestTimeout() {
    return requestTimeout;
  }

  public void setRequestTimeout(int requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  public OllamaAPI getOllamaAPIInstance() {
    OllamaAPI api = new OllamaAPI(url);
    api.setRequestTimeoutSeconds(requestTimeout);
    return api;
  }
}
