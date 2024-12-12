package io.github.ollama4j.webui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Badge extends Span implements HasTheme {

    private Component icon;

    public Badge() {
        addThemeName("badge");
    }

    public Badge(String text) {
        this();
        add(new Span(text));
    }

    public Badge(String text, BadgeVariant... variants) {
        this(text);
        addThemeVariants(variants);
    }

    public void setIcon(LineAwesomeIcon icon) {
        if (this.icon != null) {
            remove(this.icon);
        }
        this.icon = icon.create();
        this.icon.addClassNames(Padding.XSMALL);
        addComponentAsFirst(this.icon);
    }

    public void addThemeVariants(BadgeVariant... variants) {
        getThemeNames().addAll(Stream.of(variants).map(BadgeVariant::getVariantName).collect(Collectors.toList()));
    }

    public enum BadgeVariant  {
        CONTRAST("contrast"),
        ERROR("error"),
        PILL("pill"),
        PRIMARY("primary"),
        SMALL("small"),
        SUCCESS("success"),
        WARNING("warning");

        private final String variant;

        BadgeVariant(String variant) {
            this.variant = variant;
        }

        /**
         * Gets the variant name.
         *
         * @return variant name
         */
        public String getVariantName() {
            return variant;
        }
    }
}