package dev.hugog.minecraft.wonderquests.chat.summaries;

import com.google.inject.Inject;
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

  @Inject
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
              .appendNewline()
              .append(Component.text(quest.getName(), NamedTextColor.GREEN)
                  .decorate(TextDecoration.BOLD))
              .append(Component.text(" (ID: " + quest.getId() + ")", NamedTextColor.GRAY))
              .appendNewline()
              .appendNewline()
              .append(Component.text(quest.getDescription(), NamedTextColor.GRAY))
              .appendNewline()
          );

          // Objective
          player.sendMessage(Component.empty()
              .appendSpace()
              .appendSpace()
              .append(messaging.getLocalizedChatNoPrefix("summary.quest_details.objective.title")
                  .color(NamedTextColor.GREEN))
              .appendSpace()
              .append(quest.getObjective() != null
                  ? Component.text(quest.getObjective().obtainRepresentation(), NamedTextColor.GRAY)
                  : messaging.getLocalizedChatNoPrefix("summary.quest_details.objective.none"))
              .appendNewline()
          );

          // Reward
          player.sendMessage(Component.empty()
              .appendSpace()
              .appendSpace()
              .append(messaging.getLocalizedChatNoPrefix("summary.quest_details.reward.title")
                  .color(NamedTextColor.GREEN))
              .appendNewline()
          );

          if (quest.getRewards().isEmpty()) {
            player.sendMessage(Component.empty()
                .appendSpace()
                .appendSpace()
                .appendSpace()
                .append(
                    messaging.getLocalizedChatNoPrefix("summary.quest_details.reward.none"))
            );
          } else {
            quest.getRewards().forEach((reward) -> {
              player.sendMessage(Component.empty()
                  .appendSpace()
                  .appendSpace()
                  .appendSpace()
                  .append(Component.text("• ", NamedTextColor.GREEN))
                  .append(Component.text(reward.obtainRepresentation(), NamedTextColor.GRAY))
              );
            });
          }

          player.sendMessage(Component.empty());

          // Requirements
          player.sendMessage(Component.empty()
              .appendSpace()
              .appendSpace()
              .append(
                  messaging.getLocalizedChatNoPrefix("summary.quest_details.requirements.title")
                      .color(NamedTextColor.GREEN))
              .appendNewline()
          );

          if (quest.getRequirements().isEmpty()) {
            player.sendMessage(Component.empty()
                .appendSpace()
                .appendSpace()
                .appendSpace()
                .append(messaging.getLocalizedChatNoPrefix(
                    "summary.quest_details.requirements.none"))
            );
          } else {
            quest.getRequirements().forEach((requirement) -> {
              player.sendMessage(Component.empty()
                  .appendSpace()
                  .appendSpace()
                  .appendSpace()
                  .append(Component.text("• ", NamedTextColor.GREEN))
                  .append(Component.text(requirement.obtainRepresentation(), NamedTextColor.GRAY))
              );
            });
          }

          player.sendMessage(Component.empty());
          player.sendMessage(messaging.getChatSeparator());

        });

  }

}
