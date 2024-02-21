package io.github.amithkoujalgi.views.chat;

import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationMessageInput;
import com.vaadin.collaborationengine.CollaborationMessageList;
import com.vaadin.collaborationengine.MessageManager;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Aside;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;
import io.github.amithkoujalgi.views.MainLayout;
import java.util.UUID;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ChatView extends HorizontalLayout {

  public static class ChatTab extends Tab {
    private final ChatInfo chatInfo;

    public ChatTab(ChatInfo chatInfo) {
      this.chatInfo = chatInfo;
    }

    public ChatInfo getChatInfo() {
      return chatInfo;
    }
  }

  public static class ChatInfo {
    private String name;
    private int unread;
    private Span unreadBadge;

    private ChatInfo(String name, int unread) {
      this.name = name;
      this.unread = unread;
    }

    public void resetUnread() {
      unread = 0;
      updateBadge();
    }

    public void incrementUnread() {
      unread++;
      updateBadge();
    }

    private void updateBadge() {
      unreadBadge.setText(unread + "");
      unreadBadge.setVisible(unread != 0);
    }

    public void setUnreadBadge(Span unreadBadge) {
      this.unreadBadge = unreadBadge;
      updateBadge();
    }

    public String getCollaborationTopic() {
      return "chat/" + name;
    }
  }

  private ChatInfo[] chats = new ChatInfo[] {new ChatInfo("general", 0)};
  private ChatInfo currentChat = chats[0];
  private Tabs tabs;

  public ChatView() {
    addClassNames("chat-view", Width.FULL, Display.FLEX, Flex.AUTO);
    setSpacing(false);

    // UserInfo is used by Collaboration Engine and is used to share details
    // of users to each other to able collaboration. Replace this with
    // information about the actual user that is logged, providing a user
    // identifier, and the user's real name. You can also provide the users
    // avatar by passing an url to the image as a third parameter, or by
    // configuring an `ImageProvider` to `avatarGroup`.
    UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "You");

    tabs = new Tabs();
    for (ChatInfo chat : chats) {
      // Listen for new messages in each chat so we can update the
      // "unread" count
      MessageManager mm = new MessageManager(this, userInfo, chat.getCollaborationTopic());
      mm.setMessageHandler(
          context -> {
            if (currentChat != chat) {
              chat.incrementUnread();
            }
          });
      tabs.add(createTab(chat));
    }
    tabs.setOrientation(Orientation.VERTICAL);
    tabs.addClassNames(Flex.GROW, Flex.SHRINK, Overflow.HIDDEN);

    // CollaborationMessageList displays messages that are in a
    // Collaboration Engine topic. You should give in the user details of
    // the current user using the component, and a topic Id. Topic id can be
    // any freeform string. In this template, we have used the format
    // "chat/#general".
    CollaborationMessageList list =
        new CollaborationMessageList(userInfo, currentChat.getCollaborationTopic());
    list.setSizeFull();

    // `CollaborationMessageInput is a textfield and button, to be able to
    // submit new messages. To avoid having to set the same info into both
    // the message list and message input, the input takes in the list as an
    // constructor argument to get the information from there.
    CollaborationMessageInput input = new CollaborationMessageInput(list);
    input.setWidthFull();

    // Layouting

    VerticalLayout chatContainer = new VerticalLayout();
    chatContainer.addClassNames(Flex.AUTO, Overflow.HIDDEN);

    Aside side = new Aside();
    side.addClassNames(
        Display.FLEX,
        FlexDirection.COLUMN,
        Flex.GROW_NONE,
        Flex.SHRINK_NONE,
        Background.CONTRAST_5);
    side.setWidth("18rem");
    Header header = new Header();
    header.addClassNames(
        Display.FLEX,
        FlexDirection.ROW,
        Width.FULL,
        AlignItems.CENTER,
        Padding.MEDIUM,
        BoxSizing.BORDER);
    H3 channels = new H3("Channels");
    channels.addClassNames(Flex.GROW, Margin.NONE);
    CollaborationAvatarGroup avatarGroup = new CollaborationAvatarGroup(userInfo, "chat");
    avatarGroup.setMaxItemsVisible(4);
    avatarGroup.addClassNames(Width.AUTO);

    header.add(channels, avatarGroup);

    side.add(header, tabs);

    chatContainer.add(list, input);
    add(chatContainer, side);
    setSizeFull();
    expand(list);

    // Change the topic id of the chat when a new tab is selected
    tabs.addSelectedChangeListener(
        event -> {
          currentChat = ((ChatTab) event.getSelectedTab()).getChatInfo();
          currentChat.resetUnread();
          list.setTopic(currentChat.getCollaborationTopic());
        });
  }

  private ChatTab createTab(ChatInfo chat) {
    ChatTab tab = new ChatTab(chat);
    tab.addClassNames(JustifyContent.BETWEEN);

    Span badge = new Span();
    chat.setUnreadBadge(badge);
    badge.getElement().getThemeList().add("badge small contrast");
    tab.add(new Span("#" + chat.name), badge);

    return tab;
  }

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    Page page = attachEvent.getUI().getPage();
    page.retrieveExtendedClientDetails(
        details -> {
          setMobile(details.getWindowInnerWidth() < 740);
        });
    page.addBrowserWindowResizeListener(
        e -> {
          setMobile(e.getWidth() < 740);
        });
  }

  private void setMobile(boolean mobile) {
    tabs.setOrientation(mobile ? Orientation.HORIZONTAL : Orientation.VERTICAL);
  }
}
