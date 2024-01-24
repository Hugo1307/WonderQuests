package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSession;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionBuilder;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionFormatter;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionManager;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveStep;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateQuestObjectiveAction extends AbstractAction<Boolean> {

  private final int questId;

  private final Logger logger;

  private final Messaging messaging;
  private final QuestsService questsService;
  private final InteractiveSessionManager sessionManager;

  @Inject
  public CreateQuestObjectiveAction(@Assisted CommandSender sender, @Assisted int questId,
      @Named("bukkitLogger") Logger logger,
      Messaging messaging, QuestsService questsService,
      InteractiveSessionManager sessionManager) {
    super(sender);
    this.questId = questId;
    this.logger = logger;
    this.messaging = messaging;
    this.questsService = questsService;
    this.sessionManager = sessionManager;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("actions.general.players_only"));
      return false;
    }

    return questsService.checkIfQuestExists(questId)
        .thenApply(exists -> {

          if (!exists) {
            player.sendMessage(
                messaging.getLocalizedChatWithPrefix("general.quest.not_found",
                    Component.text(questId))
            );
            return false;
          }

          QuestObjectiveDto newObjective = new QuestObjectiveDto();

          // Set the quest ID for the new requirement
          newObjective.setQuestId(questId);

          InteractiveSessionFormatter formatter = getSessionFormatter(player);
          List<InteractiveStep> sessionSteps = getSessionSteps(newObjective);
          Runnable storeObjectiveInDatabase = () -> questsService.addQuestObjective(
                  newObjective)
              .whenComplete((result, error) -> {

                if (error != null) {
                  player.sendMessage(
                      messaging.getLocalizedChatWithPrefix("actions.requirements.create.error"));
                  logger.warning("Unable to create quest requirement: " + error.getMessage());
                  return;
                }

                player.sendMessage(
                    messaging.getLocalizedChatWithPrefix("actions.requirements.create.success",
                        Component.text(questId))
                );

              });
          InteractiveSession interactiveSession = createSession(player, formatter, sessionSteps,
              storeObjectiveInDatabase);

          boolean sessionStarted = interactiveSession.startSession();

          if (!sessionStarted) {
            player.sendMessage(
                messaging.getLocalizedChatWithPrefix("interaction.error.already_in_progress"));
          }

          return sessionStarted;

        }).join();

  }

  private InteractiveSessionFormatter getSessionFormatter(Player player) {
    return InteractiveSessionFormatter.builder(player, messaging)
        .title(messaging.getLocalizedRawMessage("actions.requirements.create.interaction.title"))
        .summary(messaging.getLocalizedRawMessage("actions.requirements.create.summary"))
        .cancelMessage(
            messaging.getLocalizedRawMessage("actions.requirements.create.interaction.cancel"))
        .finalMessage(messaging.getLocalizedRawMessage("actions.requirements.create.pending",
            Component.text(questId)))
        .build();
  }

  private List<InteractiveStep> getSessionSteps(QuestObjectiveDto objectiveDto) {

    InteractiveStep requirementTypeStep = InteractiveStep.builder()
        .message(messaging.getLocalizedRawMessage("Objective Type:"))
        .hint(Component.text("place_block | break_block | craft_item | kill_mobs | kill_players",
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
            case CRAFT_ITEM -> "itemName_step";
            case KILL_MOBS -> "mobName_step";
            case KILL_PLAYERS -> "amount_step";
            case LOCATION -> null;
          };

        })
        .build();

    InteractiveStep blockNameStep = InteractiveStep.builder()
        .id("blockName_step")
        .message(Component.text("Provide the block name: "))
        .inputVerification(input -> input.matches("[a-zA-Z_]+"))
        .onValidInput(objectiveDto::setStringValue)
        .customNextStep((input) -> "amount_step")
        .build();

    InteractiveStep itemNameStep = InteractiveStep.builder()
        .id("itemName_step")
        .message(Component.text("Provide the item name: "))
        .inputVerification(input -> input.matches("[a-zA-Z_]+"))
        .onValidInput(objectiveDto::setStringValue)
        .customNextStep((input) -> "amount_step")
        .build();

    InteractiveStep mobNameStep = InteractiveStep.builder()
        .id("mobName_step")
        .message(Component.text("Provide the mob name: "))
        .inputVerification(input -> input.matches("[a-zA-Z_]+"))
        .onValidInput(objectiveDto::setStringValue)
        .customNextStep((input) -> "amount_step")
        .build();

    InteractiveStep amountStep = InteractiveStep.builder()
        .id("amount_step")
        .message(Component.text("Provide the amount: "))
        .inputVerification(input -> input.matches("[0-9]*.?[0-9]+"))
        .onValidInput(input -> objectiveDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    return List.of(requirementTypeStep, blockNameStep, itemNameStep, mobNameStep, amountStep);

  }

  private InteractiveSession createSession(Player player, InteractiveSessionFormatter formatter,
      List<InteractiveStep> steps, Runnable onSessionEnd) {

    return new InteractiveSessionBuilder(player, sessionManager)
        .withSessionFormatter(formatter)
        .withSteps(steps)
        .withSessionEndCallback(onSessionEnd)
        .build();

  }

}
