package io.github.ollama4j.webui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.github.ollama4j.webui.service.ChatService;
import io.github.ollama4j.webui.views.chat.ChatView;
import io.github.ollama4j.webui.views.chat.ChatWithImageView;
import io.github.ollama4j.webui.views.chat.DownloadedModelsView;
import io.github.ollama4j.webui.views.chat.LibraryModelsView;
import org.vaadin.lineawesome.LineAwesomeIcon;

/** The main view is a top-level placeholder for other views. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

  private H2 viewTitle;

  private final ChatService service;
  
  public MainLayout(ChatService service) {
    this.service = service;
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();

    Button darkModeToggleButton = new Button("◑", click -> {
      String toggleScript = """
        const theme = document.documentElement.getAttribute('theme');
        if (theme === 'dark') {
          document.documentElement.setAttribute('theme', '');
        } else {
          document.documentElement.setAttribute('theme', 'dark');
        }
        """;
      UI.getCurrent().getPage().executeJs(toggleScript);
    });
    darkModeToggleButton.setTooltipText("Switch UI Mode to Light or Dark");
    addToDrawer(darkModeToggleButton);

    String expressionToClearCookies =
        """
        function deleteAllCookies() {
            const cookies = document.cookie.split(";");

            for (let i = 0; i < cookies.length; i++) {
                const cookie = cookies[i];
                const eqPos = cookie.indexOf("=");
                const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
                document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
            }
        }
        deleteAllCookies();
      """;
    UI.getCurrent().getPage().executeJs(expressionToClearCookies);
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel("Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    addToNavbar(true, toggle, viewTitle);
  }

  private void addDrawerContent() {
    H1 appName = new H1("Ollama4j Web UI");
    appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
    Header header = new Header(appName);

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller, createFooter());
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();
    nav.addItem(new SideNavItem("Chat", ChatView.class, LineAwesomeIcon.COMMENTS.create()));
    nav.addItem(
        new SideNavItem("Image-Based Chat", ChatWithImageView.class, LineAwesomeIcon.COMMENT_MEDICAL_SOLID.create()));
    nav.addItem(
        new SideNavItem("Downloaded Models", DownloadedModelsView.class, LineAwesomeIcon.BRAIN_SOLID.create()));
    nav.addItem(
            new SideNavItem("Model Library", LibraryModelsView.class, LineAwesomeIcon.SNOWFLAKE_SOLID.create()));
    nav.addItem(
        new SideNavItem(
            "Give us a Star ⭐",
            "https://github.com/ollama4j/ollama4j-web-ui",
            LineAwesomeIcon.GITHUB.create()));
    return nav;
  }

  private Footer createFooter() {
    Footer layout = new Footer();
    return layout;
  }


  @Override
  protected void afterNavigation() {
    super.afterNavigation();
    viewTitle.setText(getCurrentPageTitle());
  }

  private String getCurrentPageTitle() {
    PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
    return title == null ? "" : title.value();
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    // Make a connection check but allow listing models from website
    if (!service.isConnected() &&  !(this.getContent() instanceof LibraryModelsView))  {
      UI.getCurrent().navigate(OllamaConnectionView.class);
    }
  }
}
