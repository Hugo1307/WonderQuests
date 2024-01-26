package dev.hugog.minecraft.wonderquests.chat.interaction;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * This class represents an interactive step in a chat session with a player.
 * It includes various properties and methods to manage the step, such as sending messages, verifying input, and determining the next step.
 */
@Builder
public class InteractiveStep {

  @Getter
  private String id; // The unique identifier for this step

  private Component message; // The message to be sent to the player
  private Component hint; // The hint to be sent to the player

  private Predicate<String> inputVerification; // The function to verify the player's input
  private Consumer<String> onValidInput; // The function to be called when the player's input is valid
  private Consumer<String> onInvalidInput; // The function to be called when the player's input is invalid
  @Getter
  private Function<String, String> customNextStep; // The function to determine the next step based on the player's input
  @Getter
  private boolean isTerminalStep; // Whether this step is the last step in the session

  @Getter @Setter
  private InteractiveSessionFormatter formatter; // The formatter for this step

  /**
   * Sends the step's message and hint to the player.
   *
   * @param player The player to send the message and hint to.
   */
  public void run(Player player) {

    if (message != null) {

      if (hint != null) {
        player.sendMessage(formatter != null ? formatter.getFormattedStepMessage(message, hint) : message);
      } else {
        player.sendMessage(formatter != null ? formatter.getFormattedStepMessage(message) : message);
      }

    }

  }

  /**
   * Submits the player's input for this step.
   *
   * <p>Usually called by the ChatListener when a player sends a message.</p>
   * <p>If the input is valid, the onValidInput function is called.</p>
   * <p>If the input is not valid, the onInvalidInput function is called.</p>
   *
   * @param input The player's input.
   * @return true if the input was valid, false otherwise.
   */
  public boolean submitStep(String input) {

    if (inputVerification.test(input)) {
      if (onValidInput != null) {
        onValidInput.accept(input);
      }
      return true;
    } else {
      if (onInvalidInput != null) {
        onInvalidInput.accept(input);
      }
      return false;
    }

  }

}