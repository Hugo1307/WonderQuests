package dev.hugog.minecraft.wonderquests.chat.summaries;

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

public class AvailableQuestsStatusSummary implements PluginChatSummary {

  private final ActiveQuestsService activeQuestsService;
  private final Messaging messaging;

  @Inject
  public AvailableQuestsStatusSummary(ActiveQuestsService activeQuestsService, Messaging messaging) {
    this.activeQuestsService = activeQuestsService;
    this.messaging = messaging;
  }

  @Override
  public void showToPlayer(Player player, Object... args) {

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
                .append(messaging.getLocalizedChatNoPrefix("summary.active_quests.no_quests"))
                .appendNewline()
                .appendNewline()
                .append(messaging.getChatSeparator()));
            return;
          }

          player.sendMessage(Component.empty()
              .append(messaging.getChatSeparator())
              .appendNewline()
              .appendNewline()
              .append(messaging.getLocalizedRawMessage("summary.active_quests.title").style(style -> {
                style.color(NamedTextColor.GREEN);
                style.decoration(TextDecoration.BOLD, true);
              }))
              .appendNewline()
              .appendNewline()
              .append(messaging.getLocalizedChatNoPrefix("summary.active_quests.description",
                  Component.text(activeQuests.size(), NamedTextColor.GREEN)))
              .appendNewline());

          activeQuests.forEach((activeQuest) -> {

            Component questTimeLeft =
                activeQuest.getSecondsLeft() >= 0 ? messaging.getLocalizedChatNoPrefix(
                    "summary.active_quests.time_left", Component.text(activeQuest.getSecondsLeft()))
                    : messaging.getLocalizedChatNoPrefix("summary.active_quests.time_left.expired")
                        .color(NamedTextColor.RED);

            Component questSummary = Component.empty()
                .appendNewline()
                .appendSpace()
                .append(Component.text("• ", NamedTextColor.GREEN))
                .append(
                    Component.text(activeQuest.getQuestDetails().getName(), NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND,
                            "/quests details " + activeQuest.getQuestDetails().getId()))
                        .hoverEvent(
                            HoverEvent.showText(Component.text("Click to see more details"))))
                .append(Component.text(" ("))
                .append(questTimeLeft)
                .append(Component.text(")"))
                .appendSpace()
                .appendSpace()
                .append(Component.text("[", NamedTextColor.RED))
                .append(messaging.getLocalizedRawMessage("summary.active_quests.cancel")
                    .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND,
                        "/quests abort " + activeQuest.getQuestDetails().getId()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to cancel this quest")))
                    .color(NamedTextColor.RED)
                )
                .append(Component.text("]", NamedTextColor.RED))
                .appendNewline()
                .appendSpace()
                .appendSpace()
                .appendSpace()
                .append(Component.text("• ", NamedTextColor.GRAY))
                .append(messaging.getLocalizedChatNoPrefix("summary.active_quests.progress",
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
