package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelQuestAction extends AbstractAction<Boolean> {

  private final int questId;

  private final ActiveQuestsService activeQuestsService;
  private final WonderQuests plugin;
  private final ConcurrencyHandler concurrencyHandler;

  @Inject
  public CancelQuestAction(@Assisted CommandSender sender, @Assisted int questId,
      ActiveQuestsService activeQuestsService, WonderQuests plugin,
      ConcurrencyHandler concurrencyHandler) {
    super(sender);
    this.questId = questId;
    this.activeQuestsService = activeQuestsService;
    this.plugin = plugin;
    this.concurrencyHandler = concurrencyHandler;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage("Only players can create quests.");
      return false;
    }

    activeQuestsService.removeQuest(new PlayerQuestKey(player.getUniqueId(), questId));

    // Call the event to notify that the quest has been cancelled
    concurrencyHandler.run(() -> plugin.getServer().getPluginManager()
        .callEvent(new ActiveQuestUpdateEvent(player, QuestUpdateType.CANCELLED)), true);

    // TODO: Add message to messages.yml
    player.sendMessage("Quest cancelled.");

    return true;

  }

}
