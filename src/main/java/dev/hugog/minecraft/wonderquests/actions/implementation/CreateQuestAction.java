package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSession;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionBuilder;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionFormatter;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionManager;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveStep;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CreateQuestAction extends AbstractAction<Boolean> {

  private final Logger logger;
  private final Messaging messaging;
  private final InteractiveSessionManager sessionManager;
  private final QuestsService questsService;

  @Inject
  public CreateQuestAction(
      @Assisted CommandSender sender,
      @Named("bukkitLogger") Logger logger,
      Messaging messaging,
      InteractiveSessionManager sessionManager,
      QuestsService questsService
  ) {
    super(sender);
    this.logger = logger;
    this.messaging = messaging;
    this.sessionManager = sessionManager;
    this.questsService = questsService;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return false;
    }

    QuestDto questDto = new QuestDto();
    QuestObjectiveDto objectiveDto = new QuestObjectiveDto();

    InteractiveSessionFormatter formatter = InteractiveSessionFormatter.builder(player, messaging)
        .title(messaging.getLocalizedRawMessage("commands.quest.create.interaction.title"))
        .summary(messaging.getLocalizedRawMessage("commands.quest.create.summary"))
        .cancelMessage(messaging.getLocalizedRawMessage("commands.quest.create.interaction.cancel"))
        .finalMessage(messaging.getLocalizedRawMessage("commands.quest.create.interaction.pending"))
        .build();

    InteractiveStep questNameStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.name"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setName)
        .build();

    InteractiveStep questDescriptionStep = InteractiveStep.builder()
        .message(
            messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.description"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setDescription)
        .build();

    InteractiveStep questOpeningMessageStep = InteractiveStep.builder()
        .message(
            messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.opening_message"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setOpeningMsg)
        .build();

    InteractiveStep questClosingMessageStep = InteractiveStep.builder()
        .message(
            messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.closing_message"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(questDto::setClosingMsg)
        .build();

    InteractiveStep questTimeLimitStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.interaction.time_limit"))
        .inputVerification(input -> input.matches("\\d+") && Integer.parseInt(input) >= 0)
        .onValidInput(input -> questDto.setTimeLimit(Integer.parseInt(input)))
        .build();

    InteractiveStep requirementTypeStep = InteractiveStep.builder()
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.objective.type"))
        .hint(Component.text("place_block | break_block | kill_mobs",
            NamedTextColor.GRAY))
        .inputVerification(input -> ObjectiveType.fromString(input) != null)
        .onValidInput(input -> objectiveDto.setType(ObjectiveType.fromString(input)))
        .customNextStep(input -> {

          ObjectiveType objectiveType = ObjectiveType.fromString(input);

          if (objectiveType == null) {
            return null;
          }

          return switch (objectiveType) {
            case PLACE_BLOCK, BREAK_BLOCK -> "blockName_step";
            case KILL_MOBS -> "mobName_step";
            case CRAFT_ITEM, KILL_PLAYERS, LOCATION -> null;
          };

        })
        .build();

    InteractiveStep blockNameStep = InteractiveStep.builder()
        .id("blockName_step")
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.objective.block"))
        .inputVerification(
            input -> input.matches("[a-zA-Z_]+") && Material.matchMaterial(input) != null
        )
        .onValidInput(objectiveDto::setStringValue)
        .customNextStep((input) -> "amount_step")
        .build();

    InteractiveStep mobNameStep = InteractiveStep.builder()
        .id("mobName_step")
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.objective.mob"))
        .inputVerification(input -> input.matches("[a-zA-Z_]+") && EntityType.fromName(input) != null)
        .onValidInput(objectiveDto::setStringValue)
        .customNextStep((input) -> "amount_step")
        .build();

    InteractiveStep amountStep = InteractiveStep.builder()
        .id("amount_step")
        .message(messaging.getLocalizedChatNoPrefix("commands.quest.create.objective.amount"))
        .inputVerification(input -> input.matches("[0-9]*.?[0-9]+"))
        .onValidInput(input -> objectiveDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    InteractiveSession interactiveSession = new InteractiveSessionBuilder(player, sessionManager)
        .withSessionFormatter(formatter)
        .withStep(questNameStep)
        .withStep(questDescriptionStep)
        .withStep(questOpeningMessageStep)
        .withStep(questClosingMessageStep)
        .withStep(questTimeLimitStep)
        .withStep(requirementTypeStep)
        .withStep(blockNameStep)
        .withStep(mobNameStep)
        .withStep(amountStep)
        .withSessionEndCallback(() -> {
          questsService.createNewQuest(questDto)
              .thenAccept((questId) -> {
                objectiveDto.setQuestId(questId);
                questsService.addQuestObjective(objectiveDto)
                    .thenAccept((objective) -> player.sendMessage(
                        messaging.getLocalizedChatInfo("commands.quest.create.success")))
                    .exceptionally((exception) -> {
                      player.sendMessage(
                          messaging.getLocalizedChatInfo("commands.quest.create.error")
                              .color(NamedTextColor.RED)
                      );
                      logger.warning("Error creating quest objective: " + exception.getMessage());
                      return null;
                    });
              })
              .exceptionally((exception) -> {
                player.sendMessage(
                    messaging.getLocalizedChatInfo("commands.quest.create.error")
                        .color(NamedTextColor.RED)
                );
                logger.warning("Error creating quest: " + exception.getMessage());
                return null;
              });
        })
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
