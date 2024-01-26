package dev.hugog.minecraft.wonderquests.chat.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InteractiveSessionTest {

  @Mock
  private InteractiveSessionManager interactiveSessionManager;
  @Mock
  private Player targetPlayer;
  @Mock
  private InteractiveSessionFormatter interactiveSessionFormatter;
  @Mock
  private Runnable onSessionEnd;

  @Mock
  private InteractiveStep step1;
  @Mock
  private InteractiveStep step2;

  private InteractiveSession interactiveSession;
  private List<InteractiveStep> interactionSteps;

  @BeforeEach
  void setUp() {

    interactionSteps = Arrays.asList(step1, step2);

    interactiveSession = new InteractiveSession(targetPlayer, interactiveSessionManager,
        interactionSteps, interactiveSessionFormatter, onSessionEnd);

  }

  @Test
  @DisplayName("startSession() successfully starts a session")
  public void startSession_SuccessfullyStartsSession() {

    when(interactiveSessionManager.hasActiveSession(targetPlayer))
        .thenReturn(false);

    assertTrue(interactiveSession.startSession());

    verify(interactiveSessionManager).addSession(interactiveSession);
    verify(interactionSteps.get(0)).run(targetPlayer);

  }

  @Test
  @DisplayName("receivePlayerInput() successfully processes valid input")
  public void receivePlayerInput_ProcessesValidInput() {

    when(interactionSteps.get(0).submitStep("some_valid_input"))
        .thenReturn(true);

    interactiveSession.receivePlayerInput("some_valid_input");
    verify(interactiveSessionFormatter).sendFormattedInput("some_valid_input");

  }

  @Test
  @DisplayName("receivePlayerInput() successfully processes invalid input")
  public void receivePlayerInput_ProcessesInvalidInput() {

    when(interactionSteps.get(0).submitStep("invalid"))
        .thenReturn(false);

    interactiveSession.receivePlayerInput("invalid");
    verify(interactiveSessionFormatter).sendInvalidInputMessage("invalid");

  }

  @Test
  @DisplayName("receivePlayerInput() successfully cancels the session when the input is !cancel")
  public void receivePlayerInput_CancelsSession() {

    interactiveSession.receivePlayerInput("!cancel");

    verify(interactiveSessionFormatter).sendCancelMessage();
    verify(interactiveSessionManager).removeSession(interactiveSession);

  }

  @Test
  @DisplayName("getNextStep() successfully gets the next step")
  public void getNextStep_GetsNextStep() {

    when(interactionSteps.get(0).getCustomNextStep())
        .thenReturn(Function.identity());

    when(step1.getId()).thenReturn("step1");
    when(step2.getId()).thenReturn("step2");

    assertEquals(interactionSteps.get(1),
        interactiveSession.getNextStep(interactionSteps.get(0), "step2"));

  }

  @Test
  @DisplayName("getNextStep() returns null when there is no next step")
  public void getNextStep_ReturnsNullWhenNoNextStep() {

    when(interactionSteps.get(0).getCustomNextStep())
        .thenReturn(Function.identity());

    assertNull(interactiveSession.getNextStep(interactionSteps.get(0), "step3"));

  }

  @Test
  @DisplayName("finishSession() successfully finishes the session")
  public void finishSession_FinishesSession() {

    interactiveSession.finishSession();

    verify(interactiveSessionFormatter).sendFinishingMessage();
    verify(onSessionEnd).run();
    verify(interactiveSessionManager).removeSession(interactiveSession);

  }

}