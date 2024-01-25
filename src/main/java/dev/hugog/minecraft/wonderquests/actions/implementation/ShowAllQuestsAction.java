package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.summaries.AllQuestsSummary;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowAllQuestsAction extends AbstractAction<Boolean> {

  private final int page;
  private final Messaging messaging;
  private final QuestsService questsService;
  private final AllQuestsSummary allQuestsSummary;

  @Inject
  public ShowAllQuestsAction(@Assisted CommandSender sender, @Assisted int page,
      Messaging messaging, QuestsService questsService, AllQuestsSummary allQuestsSummary) {

    super(sender);
    this.page = page;
    this.messaging = messaging;
    this.questsService = questsService;
    this.allQuestsSummary = allQuestsSummary;

  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      return false;
    }

    questsService.getAllQuestsInInterval(page * 10, page * 10 + 10)
        .thenAccept(quests -> {

          if (quests.isEmpty()) {
            player.sendMessage(messaging.getLocalizedChatWithPrefix("actions.quests.all.none"));
            return;
          }

          allQuestsSummary.showToPlayer(player, quests, page);

        });

    return false;

  }

}
