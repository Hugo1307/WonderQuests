package dev.hugog.minecraft.wonderquests.interaction;

import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.List;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

@Builder(builderMethodName = "")
public class InteractiveSessionFormatter {

  private final Player player;
  private final Messaging messaging;

  private Component title;
  private Component summary;
  private Component cancelMessage;
  private Component finalMessage;

  public static InteractiveSessionFormatterBuilder builder(Player player, Messaging messaging) {
    return new InteractiveSessionFormatterBuilder().player(player).messaging(messaging);
  }

  public void sendDescriptionMessages() {

    Component titleComponent = Component.text()
        .color(NamedTextColor.GREEN)
        .append(title)
        .decorate(TextDecoration.BOLD)
        .build();

    Component summaryComponent = Component.text()
        .color(NamedTextColor.GRAY)
        .appendSpace()
        .append(summary)
        .build();

    Component interactionExplanationComponent = Component.text()
        .color(NamedTextColor.GRAY)
        .appendSpace()
        .append(messaging.getLocalizedChatNoPrefix("interaction.help.explanation"))
        .build();

    Component cancelInteractionComponent = Component.text()
        .color(NamedTextColor.GRAY)
        .appendSpace()
        .append(messaging.getLocalizedChatNoPrefix("interaction.help.cancel",
            Component.text("!cancel", NamedTextColor.RED)))
        .build();

    Component completeDescriptionComponent = Component.text()
        .appendNewline()
        .appendNewline()
        .append(messaging.getChatSeparator())
        .appendNewline()
        .appendNewline()
        .append(titleComponent)
        .appendNewline()
        .appendNewline()
        .append(summaryComponent)
        .appendNewline()
        .appendNewline()
        .append(interactionExplanationComponent)
        .appendNewline()
        .append(cancelInteractionComponent)
        .appendNewline()
        .appendNewline()
        .build();

    player.sendMessage(completeDescriptionComponent);

  }

  public void sendFinishingMessage() {

    Component finalMessageComponent = Component.text()
        .appendNewline()
        .append(messaging.getChatSeparator())
        .appendNewline()
        .append(Component.text("[!] ", NamedTextColor.GRAY))
        .color(NamedTextColor.GREEN)
        .append(finalMessage)
        .build();

    player.sendMessage(finalMessageComponent);

  }

  public void sendCancelMessage() {

    Component cancelMessageComponent = Component.text()
        .appendSpace()
        .appendSpace()
        .append(Component.text("!! ", NamedTextColor.RED))
        .color(NamedTextColor.RED)
        .append(cancelMessage)
        .appendNewline()
        .appendNewline()
        .append(messaging.getChatSeparator())
        .build();

    player.sendMessage(cancelMessageComponent);

  }

  public void sendFormattedInput(String input) {

    Component formattedInputComponent = Component.text()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .append(Component.text(">>", NamedTextColor.YELLOW))
        .append(Component.text(input, NamedTextColor.YELLOW))
        .appendNewline()
        .build();

    player.sendMessage(formattedInputComponent);

  }

  public void sendInvalidInputMessage(String input) {

    Component invalidInputComponent = Component.text()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .append(Component.text("!! ", NamedTextColor.RED))
        .append(Component.text(input, NamedTextColor.RED))
        .append(Component.text(" - ", NamedTextColor.RED))
        .color(NamedTextColor.RED)
        .append(messaging.getLocalizedRawMessage("interaction.option.invalid"))
        .appendNewline()
        .appendNewline()
        .build();

    player.sendMessage(invalidInputComponent);

  }

  public void formatStepMessages(List<InteractiveStep> steps) {
    for (InteractiveStep step : steps) {
      Component stepMessageComponent = Component.text()
          .appendSpace()
          .appendSpace()
          .append(Component.text("• ", NamedTextColor.GREEN))
          .color(NamedTextColor.GREEN)
          .append(step.getMessage())
          .appendNewline()
          .build();

      step.setMessage(stepMessageComponent);
    }
  }

}
