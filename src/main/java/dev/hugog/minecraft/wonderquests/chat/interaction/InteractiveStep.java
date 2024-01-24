package dev.hugog.minecraft.wonderquests.chat.interaction;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Builder
public class InteractiveStep {

  @Getter
  private String id;

  private Component message;
  private Component hint;

  private Predicate<String> inputVerification;
  private Consumer<String> onValidInput;
  private Consumer<String> onInvalidInput;

  @Getter
  private Function<String, String> customNextStep;
  @Getter
  private boolean isTerminalStep;

  @Getter @Setter
  private InteractiveSessionFormatter formatter;

  public void run(Player player) {

    if (message != null) {

      if (hint != null) {
        player.sendMessage(formatter != null ? formatter.getFormattedStepMessage(message, hint) : message);
      } else {
        player.sendMessage(formatter != null ? formatter.getFormattedStepMessage(message) : message);
      }

    }

  }

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
