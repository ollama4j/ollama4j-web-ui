package io.github.ollama4j.webui.views.chat;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.LibraryModel;
import io.github.ollama4j.webui.components.Badge;
import io.github.ollama4j.webui.data.LibraryModelItem;
import io.github.ollama4j.webui.data.ModelListItem;
import io.github.ollama4j.webui.service.ChatService;
import io.github.ollama4j.webui.views.MainLayout;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@PageTitle("Model Library")
@Route(value = "model-library", layout = MainLayout.class)
@Uses(Icon.class)
public class LibraryModelsView extends Div {

    private Grid<LibraryModelItem> grid;

    private ChatService chatService;

    private PullDialog pullDialog;

    private ArrayList<LibraryModelItem> items = new ArrayList<>();

    public LibraryModelsView(ChatService chatService) {
        setSizeFull();
        addClassNames("models-view");
        this.chatService = chatService;
        this.grid = new Grid<>(LibraryModelItem.class, false);

        // Configure Grid columns
        grid.addColumn(LibraryModelItem::getName)
                .setHeader("Model")
                .setSortable(true)
                .setResizable(true);
        grid.addColumn(LibraryModelItem::getDescription)
                .setHeader("Description")
                .setResizable(true);
        grid.addColumn(LibraryModelItem::getLastUpdated)
                .setHeader("Updated")
                .setSortable(true)
                .setResizable(true);
        grid.addColumn(LibraryModelItem::getPullCount)
                .setHeader("Pulls")
                .setSortable(true);
        grid.addColumn(LibraryModelItem::getTotalTags)
                .setHeader("Tags")
                .setSortable(true);


        // Action column
        final Collection<ModelListItem> loadedModels = new ArrayList<>();
        try {
            loadedModels.addAll(chatService.getModels());
        } catch (Exception ignored) { }
        grid.addComponentColumn(model -> {
            Button link = new Button("",new Icon(VaadinIcon.EXTERNAL_LINK),
                    e -> UI.getCurrent().getPage().open(model.getLink()));
            link.setTooltipText("View at ollama.com");
            Button pullButton = new Button("",new Icon(VaadinIcon.DOWNLOAD),
                    e -> openPullDialog(model));
            pullButton.setEnabled(loadedModels.stream().noneMatch(m -> model.getName().equals(m.getName())));
            pullButton.setTooltipText("Pull model locally");
            return new HorizontalLayout(pullButton,link);
        }).setHeader("");

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        // Add a listener for item selection
        grid.asSingleSelect().addValueChangeListener(event -> {
            LibraryModelItem selectedModel = event.getValue();
            if (selectedModel != null) {
            }
        });

        add(grid);
        refreshGrid();
    }

    private void openPullDialog(LibraryModelItem model) {
        // Close if already open
        if (pullDialog != null) {
            pullDialog.close();
            pullDialog = null;
        }
        pullDialog = new PullDialog(model);
        pullDialog.open();
    }

    private void refreshGrid() {
        items = new ArrayList<>(); // Todo: this could be cached
        try {
            List<LibraryModel> libraryModels = chatService.listLibraryModels();
            libraryModels.forEach(libraryModel -> {
                LibraryModelItem item = new LibraryModelItem();
                item.setName(libraryModel.getName());
                item.setDescription(libraryModel.getDescription());
                item.setPullCount(libraryModel.getPullCount());
                item.setTotalTags(libraryModel.getTotalTags());
                item.setLastUpdated(libraryModel.getLastUpdated());
                item.setPopularTags(libraryModel.getPopularTags());
                items.add(item);
            });
        } catch (OllamaBaseException | IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            grid.setItems(items);
        }
    }

    /** Dialog for displaying model details before pull.
     *
     */
    class PullDialog extends Dialog {

        PullDialog(LibraryModelItem model) {
            // Dialog for displaying details
            setMinWidth("300px");
            setMinHeight("300px");
            setMaxWidth("600px");
            setMaxHeight("600px");
            setCloseOnEsc(true);
            setCloseOnOutsideClick(true);

            setHeaderTitle(model.getName());
            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.setMargin(false);
            dialogContent.setPadding(false);
            dialogContent.setSizeFull();
            dialogContent.add(new Paragraph(model.getDescription()));
            dialogContent.add(new HorizontalLayout(model.getPopularTags()
                    .stream().map(t -> new Badge(t, Badge.BadgeVariant.SMALL))
                    .toList().toArray(new Badge[]{})));
            dialogContent.add(new Span("%s pulls, updated %s".formatted(model.getPullCount(),
                    model.getLastUpdated())));
            this.add(dialogContent);


            Button closeButton = new Button("Close", event -> this.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            this.add(closeButton);
            closeButton.addClickShortcut(Key.ESCAPE);
            Button pullButton = new Button("Pull", new Icon(VaadinIcon.DOWNLOAD), e -> {
                chatService.pullModel(model.getName());
                this.close();
            });

            pullButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            pullButton.addClickShortcut(Key.ENTER);
            HorizontalLayout buttons = new HorizontalLayout(closeButton, pullButton);
            buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            buttons.setWidthFull();
            this.getFooter().add(buttons);

            this.addDialogCloseActionListener(l -> {
                pullDialog = null;
            });
        }
    }
}

