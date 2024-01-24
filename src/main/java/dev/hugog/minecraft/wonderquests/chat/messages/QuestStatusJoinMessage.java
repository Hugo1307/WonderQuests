package dev.hugog.minecraft.wonderquests.chat.messages;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class QuestStatusJoinMessage {

  private final ActiveQuestsService activeQuestsService;
  private final Messaging messaging;

  @Inject
  public QuestStatusJoinMessage(ActiveQuestsService activeQuestsService, Messaging messaging) {
    this.activeQuestsService = activeQuestsService;
    this.messaging = messaging;
  }

  public void displayActiveQuestsSummary(Player player) {

    activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId())
        .thenAccept((activeQuestsSet) -> {

          List<ActiveQuestDto> activeQuests = activeQuestsSet.stream()
              .sorted((a, b) -> (int) (b.getStartedAt() - a.getStartedAt()))
              .toList();

          if (activeQuests.isEmpty()) {
            player.sendMessage(Component.empty()
                .append(messaging.getChatSeparator())
                .appendNewline()
                .appendNewline()
                .append(messaging.getLocalizedChatNoPrefix("join.quest.summary.no_quests"))
                .appendNewline()
                .appendNewline()
                .append(messaging.getChatSeparator()));
            return;
          }

          player.sendMessage(Component.empty()
              .append(messaging.getChatSeparator())
              .appendNewline()
              .appendNewline()
              .append(messaging.getLocalizedRawMessage("join.quest.summary.title").style(style -> {
                style.color(NamedTextColor.GREEN);
                style.decoration(TextDecoration.BOLD, true);
              }))
              .appendNewline()
              .appendNewline()
              .append(messaging.getLocalizedChatNoPrefix("join.quest.summary.description",
                  Component.text(activeQuests.size(), NamedTextColor.GREEN)))
              .appendNewline());

          activeQuests.forEach((activeQuest) -> {

            Component questTimeLeft =
                activeQuest.getSecondsLeft() >= 0 ? messaging.getLocalizedChatNoPrefix(
                    "join.quest.summary.time_left", Component.text(activeQuest.getSecondsLeft()))
                    : messaging.getLocalizedChatNoPrefix("join.quest.summary.time_left.expired")
                        .color(NamedTextColor.RED);

            Component questSummary = Component.empty()
                .appendNewline()
                .appendSpace()
                .append(Component.text("• ", NamedTextColor.GREEN))
                .append(
                    Component.text(activeQuest.getQuestDetails().getName(), NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND,
                            "/quests info " + activeQuest.getQuestDetails().getId()))
                        .hoverEvent(
                            HoverEvent.showText(Component.text("Click to see more details"))))
                .append(Component.text(" ("))
                .append(questTimeLeft)
                .append(Component.text(")"))
                .appendSpace()
                .appendSpace()
                .append(Component.text("[", NamedTextColor.RED))
                .append(messaging.getLocalizedRawMessage("join.quest.summary.cancel")
                    .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND,
                        "/quests cancel " + activeQuest.getQuestDetails().getId()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to cancel this quest")))
                    .color(NamedTextColor.RED)
                )
                .append(Component.text("]", NamedTextColor.RED))
                .appendNewline()
                .appendSpace()
                .appendSpace()
                .appendSpace()
                .append(Component.text("• ", NamedTextColor.GRAY))
                .append(messaging.getLocalizedChatNoPrefix("join.quest.summary.progress",
                    Component.text(activeQuest.getProgressPercentage() + "%",
                        NamedTextColor.YELLOW),
                    Component.text(activeQuest.getProgress()),
                    Component.text(activeQuest.getTarget())))
                .appendNewline();

            player.sendMessage(questSummary);
          });

          player.sendMessage(messaging.getChatSeparator());

        });

  }

}
