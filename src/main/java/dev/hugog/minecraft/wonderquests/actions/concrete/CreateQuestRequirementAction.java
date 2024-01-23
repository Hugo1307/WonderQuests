package dev.hugog.minecraft.wonderquests.actions.concrete;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.dtos.requirements.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.RequirementType;
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

public class CreateQuestRequirementAction extends AbstractAction<Boolean> {

  private final int questId;

  private final Logger logger;
  private final Messaging messaging;
  private final InteractiveSessionManager sessionManager;
  private final QuestsService questsService;

  @Inject
  public CreateQuestRequirementAction(@Assisted CommandSender sender, @Assisted int questId,
      @Named("bukkitLogger") Logger logger, Messaging messaging,
      InteractiveSessionManager sessionManager, QuestsService questsService) {

    super(sender);

    this.questId = questId;

    this.logger = logger;
    this.messaging = messaging;
    this.sessionManager = sessionManager;
    this.questsService = questsService;

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

          QuestRequirementDto newRequirement = new QuestRequirementDto();

          // Set the quest ID for the new requirement
          newRequirement.setQuestId(questId);

          InteractiveSessionFormatter formatter = getSessionFormatter(player);
          List<InteractiveStep> sessionSteps = getSessionSteps(newRequirement);
          Runnable storeRequirementInDatabase = () -> questsService.addQuestRequirement(
                  newRequirement)
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
              storeRequirementInDatabase);

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

  private List<InteractiveStep> getSessionSteps(QuestRequirementDto requirementDto) {

    InteractiveStep requirementTypeStep = InteractiveStep.builder()
        .message(messaging.getLocalizedRawMessage("actions.requirements.create.interaction.type"))
        .hint(Component.text("money | permission | item | quest_completed | quest_not_completed",
            NamedTextColor.GRAY))
        .inputVerification(input -> RequirementType.fromString(input) != null)
        .onValidInput(input -> requirementDto.setType(RequirementType.fromString(input)))
        .customNextStep(input -> {

          RequirementType requirementType = RequirementType.fromString(input);

          if (requirementType == null) {
            return null;
          }

          return switch (requirementType) {
            case MONEY -> "moneyStep";
            case PERMISSION -> "permissionStep";
            case ITEM -> "itemStep";
            case QUEST_COMPLETED -> "questCompletedStep";
            case QUEST_NOT_COMPLETED -> "questNotCompletedStep";
          };

        })

        .build();

    InteractiveStep moneyStep = InteractiveStep.builder()
        .id("moneyStep")
        .message(messaging.getLocalizedRawMessage("actions.requirements.create.interaction.money"))
        .inputVerification(input -> input.matches("[0-9]*.?[0-9]+"))
        .onValidInput(input -> requirementDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    InteractiveStep permissionStep = InteractiveStep.builder()
        .id("permissionStep")
        .message(
            messaging.getLocalizedRawMessage("actions.requirements.create.interaction.permission"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(requirementDto::setStringValue)
        .isTerminalStep(true)
        .build();

    InteractiveStep itemStep = InteractiveStep.builder()
        .id("itemStep")
        .message(messaging.getLocalizedRawMessage("actions.requirements.create.interaction.item"))
        .inputVerification(input -> input.length() > 3)
        .onValidInput(requirementDto::setStringValue)
        .isTerminalStep(true)
        .build();

    InteractiveStep questCompletedStep = InteractiveStep.builder()
        .id("questCompletedStep")
        .message(messaging.getLocalizedRawMessage(
            "actions.requirements.create.interaction.quest_completed"))
        .inputVerification(input -> input.matches("[0-9]+"))
        .onValidInput(input -> requirementDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    InteractiveStep questNotCompletedStep = InteractiveStep.builder()
        .id("questNotCompletedStep")
        .message(
            messaging.getLocalizedRawMessage("actions.requirements.create.interaction.no_quest"))
        .inputVerification(input -> input.matches("[0-9]+"))
        .onValidInput(input -> requirementDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    return List.of(requirementTypeStep, moneyStep, permissionStep, itemStep, questCompletedStep,
        questNotCompletedStep);

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
