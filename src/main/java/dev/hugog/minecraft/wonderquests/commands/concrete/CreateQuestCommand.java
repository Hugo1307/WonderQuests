package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSession;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionBuilder;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionFormatter;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionManager;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveStep;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <h3>Create Quest Command</h3>
 * <h4>Represents the command to create a new quest.</h4>
 *
 * <p>The command will be executed as follows:</p>
 * /quests create <name> <description>
 */
public class CreateQuestCommand extends AbstractPluginCommand {

  private final InteractiveSessionManager sessionManager;

  public CreateQuestCommand(CommandSender sender, String[] args, Messaging messaging,
      Object... dependencies) {
    super(sender, args, messaging, dependencies);
    this.sessionManager = (InteractiveSessionManager) dependencies[0];
  }

  @Override
  public boolean execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage("Only players can create quests.");
      return false;
    }

    QuestDto questDto = new QuestDto();

    InteractiveSessionFormatter formatter = InteractiveSessionFormatter.builder(player, messaging)
        .title(messaging.getLocalizedRawMessage("commands.quest.create.interaction.title"))
        .summary(messaging.getLocalizedRawMessage("commands.quest.create.summary"))
        .cancelMessage(messaging.getLocalizedRawMessage("commands.quest.create.interaction.cancel"))
        .finalMessage(messaging.getLocalizedRawMessage("commands.quest.create.success"))
        .build();

    InteractiveStep questNameStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.name"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setName)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questDescriptionStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.description"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setDescription)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questOpeningMessageStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.opening_message"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setOpeningMsg)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questClosingMessageStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.closing_message"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setClosingMsg)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questTimeLimitStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.time_limit"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(input -> System.out.println("Valid input: " + input))
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveSession interactiveSession = new InteractiveSessionBuilder(player, sessionManager)
        .withSessionFormatter(formatter)
        .withStep(questNameStep)
        .withStep(questDescriptionStep)
        .withStep(questOpeningMessageStep)
        .withStep(questClosingMessageStep)
        .withStep(questTimeLimitStep)
        .build();

    interactiveSession.start();

    return true;

  }

}
