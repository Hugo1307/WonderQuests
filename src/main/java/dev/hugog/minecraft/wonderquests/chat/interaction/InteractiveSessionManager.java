package dev.hugog.minecraft.wonderquests.chat.interaction;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@Singleton
public class InteractiveSessionManager {

  private final List<InteractiveSession> activeInteractiveSessions;

  public InteractiveSessionManager() {
    this.activeInteractiveSessions = new ArrayList<>();
  }

  public void notifyPlayerInput(Player player, String input) {

    activeInteractiveSessions.stream()
        .filter(interactiveSession -> interactiveSession.getTargetPlayer().equals(player))
        .findFirst()
        .ifPresent(interactiveSession -> interactiveSession.receivePlayerInput(input));

  }

  public void removeSession(InteractiveSession interactiveSession) {
    this.activeInteractiveSessions.remove(interactiveSession);
  }

  public boolean hasActiveSession(Player player) {
    return activeInteractiveSessions.stream()
        .anyMatch(interactiveSession -> interactiveSession.getTargetPlayer().equals(player));
  }

  public void addSession(InteractiveSession interactiveSession) {
    this.activeInteractiveSessions.add(interactiveSession);
  }

}
