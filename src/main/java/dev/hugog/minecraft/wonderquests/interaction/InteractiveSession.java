package dev.hugog.minecraft.wonderquests.interaction;

import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Getter
public class InteractiveSession {

  private final InteractiveSessionManager interactiveSessionManager;

  private final Player targetPlayer; // The player interacting with the chat
  private final List<InteractiveStep> interactionSteps;
  private final Component cancelMessage;

  private int currentStepIdx;

  InteractiveSession(Player targetPlayer, InteractiveSessionManager interactiveSessionManager,
      List<InteractiveStep> interactionSteps, Component cancelMessage) {

    this.targetPlayer = targetPlayer;
    this.interactiveSessionManager = interactiveSessionManager;
    this.interactionSteps = interactionSteps;
    this.cancelMessage = cancelMessage;

    this.currentStepIdx = 0;

  }

  public void start() {
    interactiveSessionManager.addSession(this);
    interactionSteps.get(currentStepIdx).run(targetPlayer);
  }

  public void receivePlayerInput(String playerInput) {

    if (playerInput.equalsIgnoreCase("!cancel")) {
      targetPlayer.sendMessage(cancelMessage);
      interactiveSessionManager.removeSession(this);
      return;
    }

    boolean completedStep = interactionSteps.get(currentStepIdx).submitStep(playerInput);

    if (completedStep) {
      if (++currentStepIdx < interactionSteps.size()) {
        interactionSteps.get(currentStepIdx).run(targetPlayer);
      } else {
        interactiveSessionManager.removeSession(this);
      }
    }

  }

}
