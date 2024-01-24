package dev.hugog.minecraft.wonderquests.chat.interaction;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class InteractiveSessionBuilder {

  private final InteractiveSessionManager interactiveSessionManager;
  private final Player targetPlayer;
  private final List<InteractiveStep> interactionSteps;
  private InteractiveSessionFormatter interactiveSessionFormatter;
  private Runnable onSessionEnd;

  public InteractiveSessionBuilder(Player targetPlayer,
      InteractiveSessionManager interactiveSessionManager) {
    this.interactiveSessionManager = interactiveSessionManager;
    this.targetPlayer = targetPlayer;
    this.interactionSteps = new ArrayList<>();
  }

  public InteractiveSessionBuilder withStep(InteractiveStep interactionStep) {
    this.interactionSteps.add(interactionStep);
    return this;
  }

  public InteractiveSessionBuilder withSteps(InteractiveStep... interactionSteps) {
    this.interactionSteps.addAll(List.of(interactionSteps));
    return this;
  }

  public InteractiveSessionBuilder withSteps(List<InteractiveStep> interactionSteps) {
    this.interactionSteps.addAll(interactionSteps);
    return this;
  }

  public InteractiveSessionBuilder withSessionFormatter(
      InteractiveSessionFormatter interactiveSessionFormatter) {
    this.interactiveSessionFormatter = interactiveSessionFormatter;
    return this;
  }

  public InteractiveSessionBuilder withSessionEndCallback(Runnable onSessionEnd) {
    this.onSessionEnd = onSessionEnd;
    return this;
  }

  public InteractiveSession build() {
    return new InteractiveSession(targetPlayer, interactiveSessionManager, interactionSteps,
        interactiveSessionFormatter, onSessionEnd);
  }
}