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

  private int currentStepIdx;

  InteractiveSession(Player targetPlayer, InteractiveSessionManager interactiveSessionManager,
      List<InteractiveStep> interactionSteps, InteractiveSessionFormatter interactiveSessionFormatter) {

    this.targetPlayer = targetPlayer;
    this.interactiveSessionManager = interactiveSessionManager;
    this.interactionSteps = interactionSteps;
    this.interactiveSessionFormatter = interactiveSessionFormatter;

    this.currentStepIdx = 0;

  }

  public void start() {

    interactiveSessionManager.addSession(this);
    interactiveSessionFormatter.sendDescriptionMessages();

    interactiveSessionFormatter.formatStepMessages(interactionSteps);

    interactionSteps.get(currentStepIdx).run(targetPlayer);

  }

  public void receivePlayerInput(String playerInput) {

    if (playerInput.equalsIgnoreCase("!cancel")) {
      interactiveSessionFormatter.sendCancelMessage();
      interactiveSessionManager.removeSession(this);
      return;
    }

    // If the current step input was valid
    boolean completedStep = interactionSteps.get(currentStepIdx).submitStep(playerInput);

    if (completedStep) {

      interactiveSessionFormatter.sendFormattedInput(playerInput);

      if (++currentStepIdx < interactionSteps.size()) {
        interactionSteps.get(currentStepIdx).run(targetPlayer);
      } else {
        interactiveSessionFormatter.sendFinishingMessage();
        interactiveSessionManager.removeSession(this);
      }

    } else {
      interactiveSessionFormatter.sendInvalidInputMessage(playerInput);
    }

  }

}
