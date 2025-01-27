package io.github.ollama4j.webui.views.chat;

import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import io.github.ollama4j.webui.data.ModelItem;
import io.github.ollama4j.webui.service.ChatService;
import io.github.ollama4j.webui.views.MainLayout;
import io.github.ollama4j.exceptions.OllamaBaseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting
 * message in a notification.
 */
@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ChatView extends VerticalLayout {

  private ChatService chatService;

  private MessageList chat;
  private MessageInput input;
  private String modelSelected;
  private List<MessageListItem> chatEntries = new ArrayList<>();

  public ChatView(ChatService chatService) {
    this.chatService = chatService;
    //    H5 header = new H5("Model: " + chatService.getOllamaModel());

    ComboBox<ModelItem> modelsDropdown = new ComboBox<>("Models");
    try {
      modelsDropdown.setItems(chatService.getModelItems());
    } catch (OllamaBaseException | IOException | URISyntaxException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    modelsDropdown.setItemLabelGenerator(ModelItem::getName);
    modelsDropdown.setWidthFull();
    modelsDropdown.setMaxWidth("1200px");

    try {
      Optional<ModelItem> model = chatService.getModelItems().stream().findFirst();
      if (model.isPresent()) {
        modelsDropdown.setValue(new ModelItem(model.get().getName(), model.get().getVersion()));
        modelSelected = model.get().getName();
      }
    } catch (OllamaBaseException | IOException | URISyntaxException | InterruptedException e) {
      throw new RuntimeException(e);
    }

    modelsDropdown.addValueChangeListener(
            event -> {
              MessageListItem welcome =
                      new MessageListItem(
                              String.format(
                                      "Hi! I'm %s. How may I assist you today?", event.getValue().getName()),
                              Instant.now(),
                              "AI");
              welcome.setUserAbbreviation("AI");
              welcome.setUserColorIndex(2);
              chat.setItems(welcome);
              chatEntries.clear();
              chatService.clearMessages();
              modelSelected = event.getValue().getName();
              //          header.setText(modelSelected);
            });

//    add(modelsDropdown);

    chat = new MessageList();

    MessageListItem welcome =
            new MessageListItem(
                    "Hello there! Select a model to start chatting with AI.", Instant.now(), "AI");
    welcome.setUserAbbreviation("AI");
    welcome.setUserColorIndex(2);

    input = new MessageInput();
    input.setI18n(new MessageInputI18n().setMessage("Ask anything").setSend("Ask"));

    chat.setItems(welcome);
    //    add(header, chat, input);
    add(modelsDropdown, chat, input);
    input.addSubmitListener(this::onSubmit);
    this.setHorizontalComponentAlignment(Alignment.CENTER, modelsDropdown, chat, input);
    this.setPadding(true);
    this.setHeightFull();
    chat.setSizeFull();
    input.setWidthFull();
    chat.setMaxWidth("1200px");
    input.setMaxWidth("1200px");
    this.chatService = chatService;
  }

  private void onSubmit(MessageInput.SubmitEvent submitEvent) {
    MessageListItem question = new MessageListItem(submitEvent.getValue(), Instant.now(), "You");
    question.setUserAbbreviation("You");
    question.setUserColorIndex(1);
    chatEntries.add(question);
    MessageListItem answer = new MessageListItem("Thinking...", Instant.now(), "AI");
    chatEntries.add(answer);
    answer.setUserAbbreviation("AI");
    answer.setUserColorIndex(2);
    chat.setItems(chatEntries);

    Thread t =
            new Thread(
                    () ->
                            chatService.ask(
                                    submitEvent.getValue(),
                                    modelSelected,
                                    (s) -> getUI().ifPresent(ui -> ui.access(() -> {
                                      answer.setText(s);
                                      String jsCode = "document.querySelector('vaadin-message-list vaadin-message:last-child').scrollIntoView({ behavior: 'smooth' });";
                                      ui.getPage().executeJs(jsCode);
                                    }))));
    t.start();
  }
}
