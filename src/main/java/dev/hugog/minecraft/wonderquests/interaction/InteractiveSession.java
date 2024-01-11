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

    InteractiveStep firstStep = interactionSteps.get(0);
    firstStep.run(targetPlayer);

    return true;

  }

  public void receivePlayerInput(String playerInput) {

    if (playerInput.equalsIgnoreCase("!cancel")) {
      this.cancelSession();
      return;
    }

    InteractiveStep currentStep = interactionSteps.get(currentStepIdx);

    // If the current step input was valid
    boolean wasInputValid = currentStep.submitStep(playerInput);

    if (!wasInputValid) {
      interactiveSessionFormatter.sendInvalidInputMessage(playerInput);
      return;
    }

    interactiveSessionFormatter.sendFormattedInput(playerInput);

    // We reached the end of the steps
    if (currentStepIdx > interactionSteps.size()) {
      this.finishSession();
      return;
    }

    InteractiveStep nextStep = this.getNextStep(currentStep, playerInput);
    if (nextStep != null) {
      currentStepIdx = interactionSteps.indexOf(nextStep);
      nextStep.run(targetPlayer);
    } else { // If the next step could not be found, we finish the session
      this.finishSession();
    }

  }

  private InteractiveStep getNextStep(InteractiveStep currentStep, String playerInput) {

    // If there is no branching condition, we just go to the next step
    if (currentStep.getBranchingCondition() == null) {
      return interactionSteps.get(currentStepIdx+1);
    }

    // Otherwise, we apply the branching condition to get the next step id
    String nextStepId = currentStep.getBranchingCondition().apply(playerInput);

    // We get the next step based on the id
    return interactionSteps.stream()
        .filter(step -> step.getId().equals(nextStepId))
        .findFirst()
        .orElse(null);

  }

  private void finishSession() {

    interactiveSessionFormatter.sendFinishingMessage();

    if (onSessionEnd != null) {
      onSessionEnd.run();
    }

    interactiveSessionManager.removeSession(this);

  }

  private void cancelSession() {
    interactiveSessionFormatter.sendCancelMessage();
    interactiveSessionManager.removeSession(this);
  }

}
