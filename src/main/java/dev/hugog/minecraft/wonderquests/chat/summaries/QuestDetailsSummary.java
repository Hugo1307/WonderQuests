package dev.hugog.minecraft.wonderquests.chat.summaries;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class QuestDetailsSummary implements PluginChatSummary {

  private final QuestsService questsService;
  private final Messaging messaging;

  public QuestDetailsSummary(QuestsService questsService, Messaging messaging) {
    this.questsService = questsService;
    this.messaging = messaging;
  }

  @Override
  public void showToPlayer(Player player, Object... args) {

    // The first argument is the quest ID
    int questId = (int) args[0];

    questsService.getQuestById(questId)
        .thenAccept((questOptional) -> {

          if (questOptional.isEmpty()) {
            player.sendMessage(messaging.getLocalizedChatNoPrefix("general.quest.not_found"));
            return;
          }

          QuestDto quest = questOptional.get();

          // Header
          player.sendMessage(Component.empty()
              .append(messaging.getChatSeparator())
              .appendNewline()
              .append(Component.text(quest.getName(), NamedTextColor.GREEN)
                  .decorate(TextDecoration.BOLD))
              .appendNewline()
              .appendNewline()
              .append(Component.text(quest.getDescription(), NamedTextColor.GRAY))
              .appendNewline()
              .appendNewline()
          );

          // Objective
          player.sendMessage(Component.empty()
              .appendSpace()
              .appendSpace()
              .append(messaging.getLocalizedRawMessage("summary.quest_details.objective.title"))
              .appendNewline()
              .appendSpace()
              .appendSpace()
              .appendSpace()
              .append(
                  Component.text(quest.getObjective().obtainRepresentation(), NamedTextColor.GRAY))
          );

          // Reward
          player.sendMessage(Component.empty()
              .appendSpace()
              .appendSpace()
              .append(messaging.getLocalizedRawMessage("summary.quest_details.reward.title"))
              .appendNewline()
          );

          quest.getRewards().forEach((reward) -> {
            player.sendMessage(Component.empty()
                .appendSpace()
                .appendSpace()
                .appendSpace()
                .append(Component.text("• ", NamedTextColor.GREEN))
                .append(Component.text(reward.obtainRepresentation(), NamedTextColor.GRAY))
            );
          });

          // Requirements
          player.sendMessage(Component.empty()
              .appendSpace()
              .appendSpace()
              .append(messaging.getLocalizedRawMessage("summary.quest_details.requirements.title"))
              .appendNewline()
          );

          quest.getRequirements().forEach((requirement) -> {
            player.sendMessage(Component.empty()
                .appendSpace()
                .appendSpace()
                .appendSpace()
                .append(Component.text("• ", NamedTextColor.GREEN))
                .append(Component.text(requirement.obtainRepresentation(), NamedTextColor.GRAY))
            );
          });

        });

  }

}
