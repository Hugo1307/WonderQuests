package dev.hugog.minecraft.wonderquests.chat.interaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractiveSessionManagerTest {

  @Mock
  private Player targetPlayer;
  @Mock
  private InteractiveSession interactiveSession;

  private InteractiveSessionManager interactiveSessionManager;

  @BeforeEach
  void setUp() {
    interactiveSessionManager = new InteractiveSessionManager();
  }

  @Test
  @DisplayName("notifyPlayerInput() successfully notifies the player's input")
  public void notifyPlayerInput_SuccessfullyNotifiesPlayerInput() {

    when(interactiveSession.getTargetPlayer())
        .thenReturn(targetPlayer);

    interactiveSessionManager.addSession(interactiveSession);
    interactiveSessionManager.notifyPlayerInput(targetPlayer, "input");
    verify(interactiveSession).receivePlayerInput("input");

  }

  @Test
  @DisplayName("removeSession() successfully removes a session")
  public void removeSession_SuccessfullyRemovesSession() {

    interactiveSessionManager.addSession(interactiveSession);
    interactiveSessionManager.removeSession(interactiveSession);

    assertFalse(interactiveSessionManager.hasActiveSession(targetPlayer));

  }

  @Test
  @DisplayName("hasActiveSession() returns true when the player has an active session")
  public void hasActiveSession_ReturnsTrueWhenPlayerHasActiveSession() {

    when(interactiveSession.getTargetPlayer())
        .thenReturn(targetPlayer);

    interactiveSessionManager.addSession(interactiveSession);
    assertTrue(interactiveSessionManager.hasActiveSession(targetPlayer));

  }

  @Test
  @DisplayName("hasActiveSession() returns false when the player does not have an active session")
  public void hasActiveSession_ReturnsFalseWhenPlayerDoesNotHaveActiveSession() {

    assertFalse(interactiveSessionManager.hasActiveSession(targetPlayer));

  }

  @Test
  @DisplayName("addSession() successfully adds a session")
  public void addSession_SuccessfullyAddsSession() {

    when(interactiveSession.getTargetPlayer())
        .thenReturn(targetPlayer);
    interactiveSessionManager.addSession(interactiveSession);

    assertTrue(interactiveSessionManager.hasActiveSession(targetPlayer));

  }

}