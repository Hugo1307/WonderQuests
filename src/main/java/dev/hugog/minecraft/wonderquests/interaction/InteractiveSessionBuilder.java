package dev.hugog.minecraft.wonderquests.interaction;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public class InteractiveSessionBuilder {

  private final InteractiveSessionManager interactiveSessionManager;
  private final Player targetPlayer;
  private final List<InteractiveStep> interactionSteps;
  private InteractiveSessionFormatter interactiveSessionFormatter;

  public InteractiveSessionBuilder(Player targetPlayer, InteractiveSessionManager interactiveSessionManager) {
    this.interactiveSessionManager = interactiveSessionManager;
    this.targetPlayer = targetPlayer;
    this.interactionSteps = new ArrayList<>();
  }

  public InteractiveSessionBuilder withStep(InteractiveStep interactionStep) {
    this.interactionSteps.add(interactionStep);
    return this;
  }

  public InteractiveSessionBuilder withSessionFormatter(InteractiveSessionFormatter interactiveSessionFormatter) {
    this.interactiveSessionFormatter = interactiveSessionFormatter;
    return this;
  }

  public InteractiveSession build() {
    return new InteractiveSession(targetPlayer, interactiveSessionManager, interactionSteps, interactiveSessionFormatter);
  }
}