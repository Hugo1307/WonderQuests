package dev.hugog.minecraft.wonderquests.chat.summaries;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class AdminQuestDetailsSummary implements PluginChatSummary {

  private final Logger logger;
  private final QuestsService questsService;
  private final Messaging messaging;

  @Inject
  public AdminQuestDetailsSummary(@Named("bukkitLogger") Logger logger, QuestsService questsService,
      Messaging messaging) {

    this.logger = logger;
    this.questsService = questsService;
    this.messaging = messaging;

  }

  @Override
  public void showToPlayer(Player player, Object... args) {

    if (args.length < 1 || !(args[0] instanceof Integer)) {
      logger.warning("Invalid arguments passed to AdminQuestDetailsSummary.");
      return;
    }

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
              .appendNewline()
              .append(Component.text("[", NamedTextColor.RED)
                  .append(messaging.getLocalizedRawMessage("summary.quest_details.admin.delete")
                      .color(NamedTextColor.RED)
                  )
                  .append(Component.text("]", NamedTextColor.RED))
                  .clickEvent(ClickEvent.clickEvent(Action.SUGGEST_COMMAND,
                      "/quests delete " + quest.getId())
                  )
                  .hoverEvent(HoverEvent.showText(
                      messaging.getLocalizedChatNoPrefix(
                          "summary.quest_details.admin.quest.delete.hover")
                  ))
              )
              .appendSpace()
              .append(
                  Component.text("[", NamedTextColor.YELLOW)
                      .append(
                          messaging.getLocalizedRawMessage("summary.quest_details.admin.refresh")
                              .color(NamedTextColor.YELLOW)
                      )
                      .append(Component.text("]", NamedTextColor.YELLOW))
                      .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND,
                          "/quests details " + quest.getId())
                      )
                      .hoverEvent(HoverEvent.showText(
                          messaging.getLocalizedChatNoPrefix(
                              "summary.quest_details.admin.quest.refresh.hover")
                      ))
              )
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
              .appendSpace()
              .appendSpace()
              .append(Component.text("[", NamedTextColor.BLUE)
                  .append(messaging.getLocalizedRawMessage("summary.quest_details.admin.add")
                      .color(NamedTextColor.BLUE)
                  )
                  .append(Component.text("]", NamedTextColor.BLUE))
                  .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                      "/quests reward create " + quest.getId()))
                  .hoverEvent(HoverEvent.showText(
                      messaging.getLocalizedChatNoPrefix(
                          "summary.quest_details.admin.reward.add.hover")
                  ))
              )
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
                  .appendSpace()
                  .appendSpace()
                  .append(Component.text("[", NamedTextColor.RED)
                      .append(messaging.getLocalizedRawMessage("summary.quest_details.admin.delete")
                          .color(NamedTextColor.RED)
                      )
                      .append(Component.text("]", NamedTextColor.RED))
                      .clickEvent(ClickEvent.clickEvent(Action.SUGGEST_COMMAND,
                          "/quests reward delete " + reward.getId())
                      )
                      .hoverEvent(HoverEvent.showText(
                          messaging.getLocalizedChatNoPrefix(
                              "summary.quest_details.admin.reward.delete.hover")
                      ))
                  )
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
              .appendSpace()
              .appendSpace()
              .append(Component.text("[", NamedTextColor.BLUE)
                  .append(messaging.getLocalizedRawMessage("summary.quest_details.admin.add")
                      .color(NamedTextColor.BLUE)
                  )
                  .append(Component.text("]", NamedTextColor.BLUE))
                  .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND,
                      "/quests requirement create " + quest.getId()))
                  .hoverEvent(HoverEvent.showText(
                      messaging.getLocalizedChatNoPrefix(
                          "summary.quest_details.admin.requirement.add.hover")
                  ))
              )
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
                  .appendSpace()
                  .appendSpace()
                  .append(Component.text("[", NamedTextColor.RED)
                      .append(messaging.getLocalizedRawMessage("summary.quest_details.admin.delete")
                          .color(NamedTextColor.RED)
                      )
                      .append(Component.text("]", NamedTextColor.RED))
                      .clickEvent(ClickEvent.clickEvent(Action.SUGGEST_COMMAND,
                          "/quests requirement delete " + requirement.getId())
                      )
                      .hoverEvent(HoverEvent.showText(
                          messaging.getLocalizedChatNoPrefix(
                              "summary.quest_details.admin.requirement.delete.hover")
                      ))
                  )
              );
            });
          }

          player.sendMessage(Component.empty());
          player.sendMessage(messaging.getChatSeparator());

        });

  }

}
