package io.github.ollama4j.webui.views.chat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.webui.data.ModelItem;
import io.github.ollama4j.webui.service.ChatService;
import io.github.ollama4j.webui.views.MainLayout;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

/**
 * The main view contains a text field for getting the user name and a button that shows a greeting
 * message in a notification.
 */
@PageTitle("Document-Based Chat")
@Route(value = "document-chat", layout = MainLayout.class)
public class ChatWithDocumentView extends VerticalLayout {

    private static final List<String> CONTENT_TYPES = List.of("application/pdf",
            "text/html",
            "application/msword");

    private ChatService chatService;
    private String modelSelected;
    private final List<MessageListItem> chatEntries = new ArrayList<>();
    private final List<String> documents = new ArrayList<>();

    private final Upload upload;
    private final MessageList chat;
    private final MessageInput input;


    public ChatWithDocumentView(ChatService chatService) {
        this.chatService = chatService;
        Button resetButton = new Button("Reset");
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ComboBox<ModelItem> modelsDropdown = new ComboBox<>("Model");
        try {
            modelsDropdown.setItems(chatService.getModelItems());
        } catch (IOException | URISyntaxException | InterruptedException | OllamaBaseException e) {
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

        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setAlignItems(Alignment.END);

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        upload = new Upload(buffer);
        upload.setWidthFull();
        upload.setMaxFileSize(50 * 1024 * 1024);
        upload.setAcceptedFileTypes(CONTENT_TYPES.toArray(String[]::new));
        container.add(upload, modelsDropdown, resetButton);

        chat = new MessageList();
        input = new MessageInput();

        add(container, chat, input);
        input.addSubmitListener(this::onSubmit);
        this.setHorizontalComponentAlignment(Alignment.CENTER, container, chat, input);
        this.setPadding(true);
        this.setHeightFull();
        chat.setSizeFull();
        input.setWidthFull();
        chat.setMaxWidth("1200px");
        input.setMaxWidth("1200px");
        this.chatService = chatService;

        // Configure event listeners

        resetButton.addClickListener(e -> reset());

        modelsDropdown.addValueChangeListener(
                event -> {
                    modelSelected = event.getValue().getName();
                    MessageListItem loadedMessage =
                            new MessageListItem(
                                    String.format(
                                            "Loaded model %s.", event.getValue().getName()),
                                    Instant.now(),
                                    "AI");
                    loadedMessage.setUserAbbreviation("AI");
                    loadedMessage.setUserColorIndex(2);
                    chatEntries.add(loadedMessage);
                    chat.setItems(chatEntries);
                });

        upload.addSucceededListener(
                event -> {
                    String fileName = event.getFileName();
                    InputStream inputStream = buffer.getInputStream(fileName);
                    documents.add(detectAndParseText(inputStream));

                    MessageListItem loadedMessage =
                            new MessageListItem(
                                    String.format(
                                            "Uploaded file %s.", fileName),
                                    Instant.now(),
                                    "AI");
                    loadedMessage.setUserAbbreviation("AI");
                    loadedMessage.setUserColorIndex(2);
                    chatEntries.add(loadedMessage);
                    chat.setItems(chatEntries);

                });


        // Reset
        reset();
    }

    private void reset() {
        documents.clear();
        upload.clearFileList();
        chatEntries.clear();
        chatService.clearMessages();

        MessageListItem resetMessage =
                new MessageListItem(
                        "Hello there! Upload documents to start chatting with AI.", Instant.now(), "AI");
        resetMessage.setUserAbbreviation("AI");
        resetMessage.setUserColorIndex(2);
        chat.setItems(resetMessage);


    }

    private static String detectAndParseText(InputStream inputStream) {
        Tika tika = new Tika();
        try (TikaInputStream tis = TikaInputStream.get(inputStream)) {
            String mimeType = tika.detect(tis);
            if (CONTENT_TYPES.contains(mimeType)) {
                AutoDetectParser parser = new AutoDetectParser();
                ContentHandler handler = new BodyContentHandler();
                parser.parse(tis, handler, new Metadata());
                return handler.toString();
            } else {
                throw new RuntimeException("Unsupported file type: " + mimeType);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing PDF", ex);
        }
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
                        () -> chatService.ask(createDocumentPrompt(submitEvent.getValue()), modelSelected,
                                (s) -> getUI().ifPresent(ui -> ui.access(() -> answer.setText(s)))));
        t.start();
    }

    private String createDocumentPrompt(String question) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Answer the question about the following document(s):\n");
        for (String document : documents) {
            prompt.append("-- Start document content: ").append(document).append("\n-- End of document content --\n");
        }
        prompt.append(question);
        return prompt.toString();
    }

}
