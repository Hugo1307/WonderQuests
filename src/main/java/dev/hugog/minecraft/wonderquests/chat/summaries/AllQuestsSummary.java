package dev.hugog.minecraft.wonderquests.chat.summaries;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class AllQuestsSummary implements PluginChatSummary {

  private final Logger logger;
  private final Messaging messaging;

  @Inject
  public AllQuestsSummary(@Named("bukkitLogger") Logger logger, Messaging messaging) {
    this.messaging = messaging;
    this.logger = logger;
  }

  @Override
  public void showToPlayer(Player player, Object... args) {

    if (!(args[0] instanceof List<?> quests) || quests.isEmpty()
        || !(quests.iterator().next() instanceof QuestDto)) {
      logger.warning("Invalid 'quests' argument passed to AllQuestsSummary.");
      return;
    }

    if (args.length < 2 || !(args[1] instanceof Integer page)) {
      logger.warning("Invalid 'page' argument passed to AllQuestsSummary.");
      return;
    }

    player.sendMessage(messaging.getChatSeparator());

    // Send Header with title + next/previous page buttons
    player.sendMessage(
        Component.empty()
            .appendNewline()
            .append(messaging.getLocalizedRawMessage("summary.quest_list.title")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN))
            .appendNewline()
            .appendNewline()
    );

    quests.forEach(quest -> {

      QuestDto questDto = (QuestDto) quest;

      player.sendMessage(
          Component.empty()
              .append(Component.text("  " + questDto.getId() + ".  ")
                  .color(NamedTextColor.GREEN))
              .append(Component.text(questDto.getName())
                  .color(NamedTextColor.GRAY)
                  .decorate(TextDecoration.ITALIC)
                  .clickEvent(ClickEvent.clickEvent(
                      ClickEvent.Action.RUN_COMMAND,
                      "/quests details " + questDto.getId()
                  ))
                  .hoverEvent(HoverEvent.hoverEvent(
                      HoverEvent.Action.SHOW_TEXT,
                      Component.text("Click to see details")
                          .color(NamedTextColor.BLUE)
                  ))
              )
              .append(Component.empty())
      );

    });

    player.sendMessage(
        Component.empty()
            .appendNewline()
            .append(messaging.getLocalizedRawMessage("summary.quest_list.previous_page")
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/quests list " + (page >= 0 ? page - 1 : page)
                ))
                .hoverEvent(HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    messaging.getLocalizedRawMessage("summary.quest_list.previous_page.hover")
                        .color(NamedTextColor.BLUE)
                ))
            )
            .append(Component.text(" | "))
            .append(messaging.getLocalizedRawMessage("summary.quest_list.next_page")
                .color(NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.clickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/quests list " + (page + 1)
                ))
                .hoverEvent(HoverEvent.hoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    messaging.getLocalizedRawMessage("summary.quest_list.next_page.hover")
                        .color(NamedTextColor.BLUE)
                ))
            )
            .appendNewline()
    );

    player.sendMessage(messaging.getChatSeparator());

  }

}
