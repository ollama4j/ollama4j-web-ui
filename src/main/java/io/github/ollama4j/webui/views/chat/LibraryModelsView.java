package io.github.ollama4j.webui.views.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.LibraryModel;
import io.github.ollama4j.webui.data.LibraryModelItem;
import io.github.ollama4j.webui.service.ChatService;
import io.github.ollama4j.webui.views.MainLayout;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@PageTitle("Model Library")
@Route(value = "model-library", layout = MainLayout.class)
@Uses(Icon.class)
public class LibraryModelsView extends Div {

    private Grid<LibraryModelItem> grid;

    private Filters filters;

    private ChatService chatService;

    public LibraryModelsView(ChatService chatService) {
        setSizeFull();
        addClassNames("models-view");

        filters = new Filters(() -> filters.refreshGrid(), grid);
        filters.setup(chatService);

        VerticalLayout layout = new VerticalLayout(filters.createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
        this.chatService = chatService;
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER, LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div {

        private final TextField name = new TextField("Name");
        private final TextField phone = new TextField("Phone");
        private final DatePicker startDate = new DatePicker("Date of Birth");
        private final DatePicker endDate = new DatePicker();
        private final MultiSelectComboBox<String> occupations = new MultiSelectComboBox<>("Occupation");
        private final CheckboxGroup<String> roles = new CheckboxGroup<>("Role");

        private ChatService chatService;
        private Grid grid;

        public Filters(Runnable onSearch, Grid grid) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM, LumoUtility.BoxSizing.BORDER);
            name.setPlaceholder("First or last name");

            occupations.setItems("Insurance Clerk", "Mortarman", "Beer Coil Cleaner", "Scale Attendant");

            roles.setItems("Worker", "Supervisor", "Manager", "External");
            roles.addClassName("double-width");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                name.clear();
                phone.clear();
                startDate.clear();
                endDate.clear();
                occupations.clear();
                roles.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(name, phone, createDateRangeFilter(), occupations, roles, actions);
        }

        private Component createDateRangeFilter() {
            startDate.setPlaceholder("From");

            endDate.setPlaceholder("To");

            // For screen readers
            startDate.setAriaLabel("From date");
            endDate.setAriaLabel("To date");

            FlexLayout dateRangeComponent = new FlexLayout(startDate, new Text(" – "), endDate);
            dateRangeComponent.setAlignItems(FlexComponent.Alignment.BASELINE);
            dateRangeComponent.addClassName(LumoUtility.Gap.XSMALL);

            return dateRangeComponent;
        }

        public void setup(ChatService chatServiceCfg) {
            chatService = chatServiceCfg;
        }

        private Component createGrid() {
            grid = new Grid<>(LibraryModelItem.class, false);
            grid.addColumn("name").setHeader("Name").setAutoWidth(true);
            grid.addColumn("description").setHeader("Description").setAutoWidth(false).setWidth("40%");
            grid.addColumn("pullCount").setHeader("Pulls").setAutoWidth(true);
            grid.addColumn("totalTags").setHeader("Tags").setAutoWidth(true);
            grid.addColumn("popularTagsString").setHeader("Popular Tags").setAutoWidth(true);
            grid.addColumn("lastUpdated").setHeader("Updated").setAutoWidth(true);

            List<LibraryModelItem> items = new ArrayList<>();
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
//                    item.setPopularTagsString();
                    items.add(item);
                });
            } catch (OllamaBaseException | IOException | URISyntaxException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            grid.setItems(items);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

            return grid;
        }

        private void refreshGrid() {
            grid.getDataProvider().refreshAll();
        }
    }
}

