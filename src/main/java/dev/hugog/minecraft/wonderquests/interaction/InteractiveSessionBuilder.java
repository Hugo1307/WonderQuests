package dev.hugog.minecraft.wonderquests.interaction;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class InteractiveSessionBuilder {

  private final InteractiveSessionManager interactiveSessionManager;
  private final Player targetPlayer;
  private final List<InteractiveStep> interactionSteps;
  private Component cancelMessage;

  public InteractiveSessionBuilder(Player targetPlayer, InteractiveSessionManager interactiveSessionManager) {
    this.interactiveSessionManager = interactiveSessionManager;
    this.targetPlayer = targetPlayer;
    this.interactionSteps = new ArrayList<>();
  }

  public InteractiveSessionBuilder withStep(InteractiveStep interactionStep) {
    this.interactionSteps.add(interactionStep);
    return this;
  }

  public InteractiveSessionBuilder withCancelMessage(Component cancelMessage) {
    this.cancelMessage = cancelMessage;
    return this;
  }

  public InteractiveSession build() {
    return new InteractiveSession(targetPlayer, interactiveSessionManager, interactionSteps, cancelMessage);
  }
}