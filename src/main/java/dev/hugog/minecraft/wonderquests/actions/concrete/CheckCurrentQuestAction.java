package dev.hugog.minecraft.wonderquests.actions.concrete;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.services.PlayerService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckCurrentQuestAction extends AbstractAction {

  private final Messaging messaging;
  private final PlayerService playerService;

  @Inject
  public CheckCurrentQuestAction(@Assisted CommandSender sender, Messaging messaging, PlayerService playerService) {
    super(sender);
    this.messaging = messaging;
    this.playerService = playerService;
  }

  @Override
  public boolean execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("actions.general.players_only"));
      return false;
    }

    playerService.getCurrentActiveQuest(player.getUniqueId())
            .thenAccept(activeQuests -> {
              if (activeQuests.isEmpty()) {
                player.sendMessage(messaging.getLocalizedChatWithPrefix("actions.check_current_quest.no_active_quests"));
                return;
              }
              sendQuestsToPlayer(player, activeQuests);
            });

    return true;

  }

  private void sendQuestsToPlayer(Player player, Set<ActiveQuestDto> activeQuests) {
    activeQuests.forEach((activeQuest) -> {
      player.sendMessage(messaging.getChatSeparator());
      player.sendMessage(activeQuest.getQuestDetails().getName());
      player.sendMessage(activeQuest.getQuestDetails().getDescription());
      player.sendMessage("");
      player.sendMessage("Progress: " + activeQuest.getProgress());
      player.sendMessage("Time Left: " + (activeQuest.getQuestDetails().getTimeLimit() - System.currentTimeMillis()-activeQuest.getStartedAt()));
      player.sendMessage(messaging.getChatSeparator());
    });
  }

}
