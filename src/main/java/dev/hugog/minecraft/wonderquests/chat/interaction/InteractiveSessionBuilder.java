package dev.hugog.minecraft.wonderquests.chat.interaction;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * This class is a builder for InteractiveSession objects.
 * It allows for the configuration of various aspects of an InteractiveSession, such as the steps, formatter, and end callback.
 */
public class InteractiveSessionBuilder {

  private final InteractiveSessionManager interactiveSessionManager;
  private final Player targetPlayer;
  private final List<InteractiveStep> interactionSteps;
  private InteractiveSessionFormatter interactiveSessionFormatter;
  private Runnable onSessionEnd;

  /**
   * Constructor for the InteractiveSessionBuilder class.
   *
   * @param targetPlayer The player interacting with the chat.
   * @param interactiveSessionManager The manager for interactive sessions.
   */
  public InteractiveSessionBuilder(Player targetPlayer,
      InteractiveSessionManager interactiveSessionManager) {
    this.interactiveSessionManager = interactiveSessionManager;
    this.targetPlayer = targetPlayer;
    this.interactionSteps = new ArrayList<>();
  }

  /**
   * Adds a step to the interactive session.
   *
   * @param interactionStep The step to be added.
   * @return The builder instance.
   */
  public InteractiveSessionBuilder withStep(InteractiveStep interactionStep) {
    this.interactionSteps.add(interactionStep);
    return this;
  }

  /**
   * Adds multiple steps to the interactive session.
   *
   * @param interactionSteps The steps to be added.
   * @return The builder instance.
   */
  public InteractiveSessionBuilder withSteps(InteractiveStep... interactionSteps) {
    this.interactionSteps.addAll(List.of(interactionSteps));
    return this;
  }

  /**
   * Adds a list of steps to the interactive session.
   *
   * @param interactionSteps The list of steps to be added.
   * @return The builder instance.
   */
  public InteractiveSessionBuilder withSteps(List<InteractiveStep> interactionSteps) {
    this.interactionSteps.addAll(interactionSteps);
    return this;
  }

  /**
   * Sets the formatter for the interactive session.
   *
   * @param interactiveSessionFormatter The formatter to be used.
   * @return The builder instance.
   */
  public InteractiveSessionBuilder withSessionFormatter(
      InteractiveSessionFormatter interactiveSessionFormatter) {
    this.interactiveSessionFormatter = interactiveSessionFormatter;
    return this;
  }

  /**
   * Sets the end callback for the interactive session.
   *
   * @param onSessionEnd The callback to be used when the session ends.
   * @return The builder instance.
   */
  public InteractiveSessionBuilder withSessionEndCallback(Runnable onSessionEnd) {
    this.onSessionEnd = onSessionEnd;
    return this;
  }

  /**
   * Builds the interactive session with the configured parameters.
   *
   * @return The built InteractiveSession object.
   */
  public InteractiveSession build() {
    return new InteractiveSession(targetPlayer, interactiveSessionManager, interactionSteps,
        interactiveSessionFormatter, onSessionEnd);
  }
}