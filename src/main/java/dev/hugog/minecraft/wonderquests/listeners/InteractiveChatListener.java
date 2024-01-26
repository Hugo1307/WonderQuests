package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * This class listens for chat events and handles them accordingly.
 * It is used for interactive chat sessions.
 */
@Singleton
public class InteractiveChatListener implements Listener {

  private final InteractiveSessionManager interactiveSessionManager;

  /**
   * Constructor for the InteractiveChatListener class.
   *
   * @param interactiveSessionManager The manager for interactive sessions.
   */
  @Inject
  public InteractiveChatListener(InteractiveSessionManager interactiveSessionManager) {
    this.interactiveSessionManager = interactiveSessionManager;
  }

  /**
   * This method handles the AsyncChatEvent.
   *
   * @param event The AsyncChatEvent to be handled.
   */
  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerChat(AsyncChatEvent event) {

    Component text = event.originalMessage();

    // If the player is interacting with the chat, we cancel the event and notify the session
    if (text instanceof TextComponent textComponent && interactiveSessionManager
        .hasActiveSession(event.getPlayer())) {

      interactiveSessionManager.notifyPlayerInput(event.getPlayer(), textComponent.content());
      event.setCancelled(true);

    }

  }

}