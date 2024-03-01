package io.github.amithkoujalgi.views.chat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.amithkoujalgi.data.ModelItem;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.service.ChatService;
import io.github.amithkoujalgi.views.MainLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting
 * message in a notification.
 */
@PageTitle("Image-Based Chat")
@Route(value = "image-chat", layout = MainLayout.class)
@RouteAlias(value = "image-chat", layout = MainLayout.class)
public class ChatWithImageView extends VerticalLayout {

  private ChatService chatService;

  private MessageList chat;
  private MessageInput input;
  private String modelSelected;
  private List<MessageListItem> chatEntries = new ArrayList<>();
  private List<File> imageFiles = new ArrayList<>();

  public ChatWithImageView(ChatService chatService) {
    this.chatService = chatService;
    //    H5 header = new H5("Images: None");
    Button resetButton = new Button("Reset");
    resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    //    setHorizontalComponentAlignment(Alignment.END, resetButton);

    ComboBox<ModelItem> modelsDropdown = new ComboBox<>("Image Models");
    try {
      modelsDropdown.setItems(chatService.getImageModelItems());
    } catch (OllamaBaseException | IOException | URISyntaxException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    modelsDropdown.setItemLabelGenerator(ModelItem::getName);
    modelsDropdown.setWidthFull();
    modelsDropdown.setMaxWidth("1200px");
    try {
      Optional<ModelItem> model = chatService.getImageModelItems().stream().findFirst();
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

    //    add(comboBox);
    //    add(header);

    HorizontalLayout container = new HorizontalLayout();
    container.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.JustifyContent.BETWEEN);

    MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    Upload upload = new Upload(buffer);
    upload.setWidthFull();
    upload.setHeightFull();
    upload.setMaxFileSize(50 * 1024 * 1024);

    upload.addSucceededListener(
        event -> {
          String fileName = event.getFileName();
          InputStream inputStream = buffer.getInputStream(fileName);
          try {
            imageFiles.add(inputStreamToFile(inputStream));
            //            header.setText("Images: " + imageFiles.size());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    container.add(upload, resetButton);

    resetButton.addClickListener(
        event -> {
          imageFiles.clear();
          upload.clearFileList();
          chatEntries.clear();
          //          header.setText("Images: None");
          chatService.clearMessages();
        });

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
    add(modelsDropdown, container, chat, input);
    input.addSubmitListener(this::onSubmit);
    this.setHorizontalComponentAlignment(Alignment.CENTER, modelsDropdown, container, chat, input);
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
                chatService.askWithImages(
                    submitEvent.getValue(),
                    imageFiles,
                    modelSelected,
                    (s) -> getUI().ifPresent(ui -> ui.access(() -> answer.setText(s)))));
    t.start();
  }

  public static File inputStreamToFile(InputStream inputStream) throws IOException {
    File tempFile = File.createTempFile("temp-" + UUID.randomUUID(), ".tmp");
    try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }
    return tempFile;
  }
}
