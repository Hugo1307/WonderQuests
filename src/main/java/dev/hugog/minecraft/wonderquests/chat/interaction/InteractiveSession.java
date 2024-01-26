package dev.hugog.minecraft.wonderquests.chat.interaction;

import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * This class represents an interactive session with a player.
 * It manages the flow of the interaction, including starting the session, receiving player input,
 * determining the next step based on the current step and player input, and finishing or cancelling the session.
 */
@Getter
public class InteractiveSession {

  private final InteractiveSessionManager interactiveSessionManager;
  private final Player targetPlayer; // The player interacting with the chat
  private final List<InteractiveStep> interactionSteps;
  private final InteractiveSessionFormatter interactiveSessionFormatter;
  private final Runnable onSessionEnd;
  private int currentStepIdx;

  /**
   * Constructor for the InteractiveSession class.
   *
   * @param targetPlayer The player interacting with the chat.
   * @param interactiveSessionManager The manager for interactive sessions.
   * @param interactionSteps The steps for the interaction.
   * @param interactiveSessionFormatter The formatter for the interactive session.
   * @param onSessionEnd The action to be performed when the session ends.
   */
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

  /**
   * Starts the interactive session.
   * If the player already has an active session, the method returns false and does not start a new session.
   * Otherwise, it adds the session to the session manager, sends the description messages, and runs the first step.
   *
   * @return true if the session was started, false otherwise.
   */
  public boolean startSession() {

    if (interactiveSessionManager.hasActiveSession(targetPlayer)) {
      return false;
    }

    interactiveSessionManager.addSession(this);
    interactiveSessionFormatter.sendDescriptionMessages();

    // If the steps don't have a formatter, we set the one added to this session
    interactionSteps.forEach(step -> {
      if (step.getFormatter() == null) {
        step.setFormatter(interactiveSessionFormatter);
      }
    });

    InteractiveStep firstStep = interactionSteps.get(0);
    firstStep.run(targetPlayer);

    return true;

  }

  /**
   * Receives player input and processes it based on the current step.
   * If the input is "!cancel", the session is cancelled.
   * If the input is valid for the current step, the input is submitted and the next step is determined.
   * If the input is not valid, an invalid input message is sent to the player.
   *
   * @param playerInput The input from the player.
   */
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

    // If the current step is terminal, we finish the session
    if (currentStep.isTerminalStep()) {
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

  /**
   * Determines the next step based on the current step and player input.
   * If there is no branching condition, it simply returns the next step in the list.
   * If there is a branching condition, it applies the condition to the player input to determine the next step.
   *
   * @param currentStep The current step.
   * @param playerInput The input from the player.
   * @return The next interactive step, or null if there is no next step.
   */
  InteractiveStep getNextStep(InteractiveStep currentStep, String playerInput) {

    // If there is no branching condition, we just go to the next step
    if (currentStep.getCustomNextStep() == null) {
      // If we reached the end of the steps, we return null so the session is ended
      if (currentStepIdx + 1 >= interactionSteps.size()) {
        return null;
      }
      return interactionSteps.get(currentStepIdx + 1);
    }

    // Otherwise, we apply the branching condition to get the next step id
    String nextStepId = currentStep.getCustomNextStep().apply(playerInput);

    // We get the next step based on the id
    return interactionSteps.stream()
        .filter(step -> step.getId() != null)
        .filter(step -> step.getId().equals(nextStepId))
        .findFirst()
        .orElse(null);

  }

  /**
   * Finishes the session.
   * Sends a finishing message, runs the onSessionEnd action if it exists, and removes the session from the session manager.
   */
  void finishSession() {

    interactiveSessionFormatter.sendFinishingMessage();

    if (onSessionEnd != null) {
      onSessionEnd.run();
    }

    interactiveSessionManager.removeSession(this);

  }

  /**
   * Cancels the session.
   * Sends a cancellation message and removes the session from the session manager.
   */
  private void cancelSession() {
    interactiveSessionFormatter.sendCancelMessage();
    interactiveSessionManager.removeSession(this);
  }

}