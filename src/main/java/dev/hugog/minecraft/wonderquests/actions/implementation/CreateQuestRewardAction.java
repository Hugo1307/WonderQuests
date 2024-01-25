package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.RewardType;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSession;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionBuilder;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionFormatter;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveSessionManager;
import dev.hugog.minecraft.wonderquests.chat.interaction.InteractiveStep;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateQuestRewardAction extends AbstractAction<Boolean> {

  private final int questId;

  private final Logger logger;
  private final Messaging messaging;
  private final QuestsService questsService;
  private final InteractiveSessionManager sessionManager;

  @Inject
  public CreateQuestRewardAction(@Assisted CommandSender sender, @Assisted int questId,
      @Named("bukkitLogger") Logger logger, Messaging messaging, QuestsService questsService, InteractiveSessionManager sessionManager) {
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
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
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

          QuestRewardDto newReward = new QuestRewardDto();

          // Set the quest ID for the new requirement
          newReward.setQuestId(questId);

          InteractiveSessionFormatter formatter = getSessionFormatter(player);
          List<InteractiveStep> sessionSteps = getSessionSteps(newReward);
          Runnable storeRequirementInDatabase = () -> questsService.addQuestReward(newReward)
              .whenComplete((result, error) -> {

                if (error != null) {
                  player.sendMessage(
                      messaging.getLocalizedChatWithPrefix("actions.rewards.create.error"));
                  logger.warning("Unable to create quest reward: " + error.getMessage());
                  return;
                }

                player.sendMessage(
                    messaging.getLocalizedChatWithPrefix("actions.rewards.create.success",
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
        .title(messaging.getLocalizedRawMessage("actions.rewards.create.interaction.title"))
        .summary(messaging.getLocalizedRawMessage("actions.rewards.create.summary"))
        .cancelMessage(
            messaging.getLocalizedRawMessage("actions.rewards.create.interaction.cancel"))
        .finalMessage(messaging.getLocalizedRawMessage("actions.rewards.create.pending",
            Component.text(questId)))
        .build();
  }

  private List<InteractiveStep> getSessionSteps(QuestRewardDto rewardDto) {

    InteractiveStep requirementTypeStep = InteractiveStep.builder()
        .message(messaging.getLocalizedRawMessage("actions.rewards.create.interaction.type"))
        .hint(Component.text("money | items | experience | command",
            NamedTextColor.GRAY))
        .inputVerification(input -> RewardType.fromString(input) != null)
        .onValidInput(input -> rewardDto.setType(RewardType.fromString(input)))
        .customNextStep(input -> {

          RewardType rewardType = RewardType.fromString(input);

          if (rewardType == null) {
            return null;
          }

          return switch (rewardType) {
            case EXPERIENCE -> "experienceStep";
            case COMMAND -> "commandStep";
            case MONEY -> "moneyStep";
            case ITEMS -> "itemsStep";
          };

        })
        .build();

    InteractiveStep experienceStep = InteractiveStep.builder()
        .id("experienceStep")
        .message(
            messaging.getLocalizedChatNoPrefix("actions.rewards.create.interaction.experience")
        )
        .inputVerification(input -> input.matches("[0-9]*.?[0-9]+"))
        .onValidInput(input -> rewardDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    InteractiveStep commandStep = InteractiveStep.builder()
        .id("commandStep")
        .message(
            messaging.getLocalizedChatNoPrefix("actions.rewards.create.interaction.command")
        )
        .inputVerification(input -> true)
        .onValidInput(rewardDto::setStringValue)
        .isTerminalStep(true)
        .build();

    InteractiveStep moneyStep = InteractiveStep.builder()
        .id("moneyStep")
        .message(
            messaging.getLocalizedChatNoPrefix("actions.rewards.create.interaction.money")
        )
        .inputVerification(input -> input.matches("[0-9]*.?[0-9]+"))
        .onValidInput(input -> rewardDto.setNumericValue(Float.parseFloat(input)))
        .isTerminalStep(true)
        .build();

    InteractiveStep itemsStep = InteractiveStep.builder()
        .id("itemsStep")
        .message(
            messaging.getLocalizedChatNoPrefix("actions.rewards.create.interaction.item")
        )
        .inputVerification(input -> input.matches("[a-zA-Z_]+") && Material.matchMaterial(input) != null)
        .onValidInput(input -> {
          rewardDto.setStringValue(input);
          rewardDto.setNumericValue(1F);
        })
        .isTerminalStep(true)
        .build();

    return List.of(requirementTypeStep, experienceStep, commandStep, moneyStep, itemsStep);

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
