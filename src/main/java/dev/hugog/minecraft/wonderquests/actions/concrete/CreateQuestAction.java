package dev.hugog.minecraft.wonderquests.actions.concrete;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSession;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionBuilder;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionFormatter;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionManager;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveStep;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateQuestAction extends AbstractAction {

  private final Messaging messaging;
  private final InteractiveSessionManager sessionManager;
  private final QuestsService questsService;

  @Inject
  public CreateQuestAction(@Assisted CommandSender sender, Messaging messaging,
      InteractiveSessionManager sessionManager, QuestsService questsService) {
    super(sender);
    this.messaging = messaging;
    this.sessionManager = sessionManager;
    this.questsService = questsService;
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
        .message(
            messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.description"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setDescription)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questOpeningMessageStep = InteractiveStep.builder()
        .message(
            messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.opening_message"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setOpeningMsg)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questClosingMessageStep = InteractiveStep.builder()
        .message(
            messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.closing_message"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setClosingMsg)
        .onInvalidInput(input -> System.out.println("Invalid input: " + input))
        .build();

    InteractiveStep questTimeLimitStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.time_limit"))
        .inputVerification(input -> input.matches("\\d+") && Integer.parseInt(input) >= 0)
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
        .withSessionEndCallback(() -> questsService.createNewQuest(questDto))
        .build();

    boolean couldStartSession = interactiveSession.startSession();

    if (!couldStartSession) {
      player.sendMessage(
          messaging.getLocalizedChatWithPrefix("interaction.error.already_in_progress"));
      return false;
    }

    return true;

  }

}
