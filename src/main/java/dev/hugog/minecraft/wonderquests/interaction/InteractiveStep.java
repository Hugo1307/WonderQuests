package dev.hugog.minecraft.wonderquests.interaction;

import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Builder
public class InteractiveStep {

  @Getter
  @Setter
  private Component message;

  private Predicate<String> inputVerification;
  private Consumer<String> onValidInput;
  private Consumer<String> onInvalidInput;

  public void run(Player player) {
    player.sendMessage(message);
  }

  public boolean submitStep(String input) {

    if (inputVerification.test(input)) {
      onValidInput.accept(input);
      return true;
    } else {
      onInvalidInput.accept(input);
      return false;
    }

  }

}
