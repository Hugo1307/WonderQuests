package dev.hugog.minecraft.wonderquests.chat.interaction;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * This class manages interactive sessions with players.
 * It keeps track of all active sessions and provides methods to add, remove, and check for active sessions.
 * It also handles player input notifications.
 */
@Getter
@Singleton
public class InteractiveSessionManager {

  private final List<InteractiveSession> activeInteractiveSessions;

  /**
   * Constructor for the InteractiveSessionManager class.
   * Initializes the list of active interactive sessions.
   */
  public InteractiveSessionManager() {
    this.activeInteractiveSessions = new ArrayList<>();
  }

  /**
   * Notifies the interactive session of a player's input.
   * If the player has an active session, the input is passed to the session.
   *
   * @param player The player who inputted the message.
   * @param input The input from the player.
   */
  public void notifyPlayerInput(Player player, String input) {

    activeInteractiveSessions.stream()
        .filter(interactiveSession -> interactiveSession.getTargetPlayer().equals(player))
        .findFirst()
        .ifPresent(interactiveSession -> interactiveSession.receivePlayerInput(input));

  }

  /**
   * Removes an interactive session.
   * The session is removed from the list of active sessions.
   *
   * @param interactiveSession The interactive session to be removed.
   */
  public void removeSession(InteractiveSession interactiveSession) {
    this.activeInteractiveSessions.remove(interactiveSession);
  }

  /**
   * Checks if a player has an active session.
   *
   * @param player The player to check for an active session.
   * @return true if the player has an active session, false otherwise.
   */
  public boolean hasActiveSession(Player player) {
    return activeInteractiveSessions.stream()
        .anyMatch(interactiveSession -> interactiveSession.getTargetPlayer().equals(player));
  }

  /**
   * Adds an interactive session.
   * The session is added to the list of active sessions.
   *
   * @param interactiveSession The interactive session to be added.
   */
  public void addSession(InteractiveSession interactiveSession) {
    this.activeInteractiveSessions.add(interactiveSession);
  }

}