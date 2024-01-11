package dev.hugog.minecraft.wonderquests.interaction;

import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class InteractiveSession {

  private final InteractiveSessionManager interactiveSessionManager;

  private final Player targetPlayer; // The player interacting with the chat
  private final List<InteractiveStep> interactionSteps;
  private final InteractiveSessionFormatter interactiveSessionFormatter;
  private final Runnable onSessionEnd;

  private int currentStepIdx;

  InteractiveSession(Player targetPlayer, InteractiveSessionManager interactiveSessionManager,
      List<InteractiveStep> interactionSteps,
      InteractiveSessionFormatter interactiveSessionFormatter, Runnable onSessionEnd) {

    this.targetPlayer = targetPlayer;
    this.interactiveSessionManager = interactiveSessionManager;
    this.interactionSteps = interactionSteps;
    this.interactiveSessionFormatter = interactiveSessionFormatter;
    this.onSessionEnd = onSessionEnd;

    this.currentStepIdx = 0;

  }

  public boolean startSession() {

    if (interactiveSessionManager.hasActiveSession(targetPlayer)) {
      return false;
    }

    interactiveSessionManager.addSession(this);
    interactiveSessionFormatter.sendDescriptionMessages();

    interactiveSessionFormatter.formatStepMessages(interactionSteps);

    interactionSteps.get(currentStepIdx).run(targetPlayer);

    return true;

  }

  public void receivePlayerInput(String playerInput) {

    if (playerInput.equalsIgnoreCase("!cancel")) {
      this.cancelSession();
      return;
    }

    // If the current step input was valid
    boolean completedStep = interactionSteps.get(currentStepIdx).submitStep(playerInput);

    if (completedStep) {

      interactiveSessionFormatter.sendFormattedInput(playerInput);

      if (++currentStepIdx < interactionSteps.size()) {
        interactionSteps.get(currentStepIdx).run(targetPlayer);
      } else {
        this.finishSession();
      }

    } else {
      interactiveSessionFormatter.sendInvalidInputMessage(playerInput);
    }

  }

  private void finishSession() {
    interactiveSessionFormatter.sendFinishingMessage();
    onSessionEnd.run();
    interactiveSessionManager.removeSession(this);
  }

  private void cancelSession() {
    interactiveSessionFormatter.sendCancelMessage();
    interactiveSessionManager.removeSession(this);
  }

}
