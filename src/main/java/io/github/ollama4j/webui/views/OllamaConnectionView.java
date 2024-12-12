package io.github.ollama4j.webui.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.ollama4j.webui.service.ChatService;
import io.github.ollama4j.webui.views.chat.LibraryModelsView;

@Route(value = "/ollama-connection", layout = MainLayout.class)
public class OllamaConnectionView extends VerticalLayout {

    private final ChatService service;

    public OllamaConnectionView(ChatService service) {
        this.service = service;
        setSpacing(false);

        Image img = new Image("images/ollama-connection.png", "Ollama connection");
        img.setWidth("200px");
        add(img);

        if (service.isConnected()) {
            H2 header = new H2("Connected");
            header.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);
            add(header);
            // Show connection details
            ChatService.ConnectionInfo info = service.getConnectionInfo();
            Paragraph paragraph = new Paragraph("Running %d local models at %s".formatted(info.getAvailableModels().size(), info.getHost()));
            add(paragraph);
            if (info.getAvailableModels().size() ==0) {
                    add(new Button("Find models here", e -> {
                    UI.getCurrent().navigate(LibraryModelsView.class);
                }));
            }
        } else {
            H2 header = new H2("Could not connect to Ollama :(");
            header.addClassNames(LumoUtility.Margin.Top.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);
            add(header);
            ChatService.ConnectionInfo info = service.getConnectionInfo();
            add(
                    new Paragraph("Please make sure your Ollama is running at %s".formatted(info.getHost())),
                    new Button("Try again", e ->  {
                        UI.getCurrent().getPage().reload();
                    }),
                    new Paragraph("Here is example how to run Ollama in local Docker without GPU:")
            );
            Pre instructions = new Pre("""
                    docker run -d -p 11434:11434 \\
                      -v ollama:/root/.ollama \\
                      --name ollama \\
                      ollama/ollama
                    """);
            instructions.getStyle().set("text-align", "left");
            add(instructions);
            add(new Anchor("https://github.com/ollama4j/ollama4j", "More instructions"));
        }

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }
}
