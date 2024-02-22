package io.github.amithkoujalgi.views.chat;

import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import io.github.amithkoujalgi.service.ChatService;
import io.github.amithkoujalgi.views.MainLayout;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

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
  private List<MessageListItem> chatEntries = new ArrayList<>();

  public ChatView(ChatService chatService) {
    this.chatService = chatService;
    H5 header = new H5("Model: " + chatService.getOllamaModel());

    chat = new MessageList();

    MessageListItem welcome =
        new MessageListItem("Hello there! How may I assist you today?", Instant.now(), "AI");

    input = new MessageInput();
    input.setI18n(new MessageInputI18n().setMessage("Ask anything").setSend("Ask"));

    chat.setItems(welcome);
    add(header, chat, input);
    input.addSubmitListener(this::onSubmit);
    this.setHorizontalComponentAlignment(Alignment.CENTER, chat, input);
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
                    (s) -> getUI().ifPresent(ui -> ui.access(() -> answer.setText(s)))));
    t.start();
  }
}
