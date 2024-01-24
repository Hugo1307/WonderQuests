package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelQuestAction extends AbstractAction<Boolean> {

  private final int questId;

  private final ActiveQuestsService activeQuestsService;

  @Inject
  public CancelQuestAction(@Assisted CommandSender sender, @Assisted int questId,
      ActiveQuestsService activeQuestsService) {
    super(sender);
    this.questId = questId;
    this.activeQuestsService = activeQuestsService;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage("Only players can create quests.");
      return false;
    }

    activeQuestsService.removeQuest(new PlayerQuestKey(player.getUniqueId(), questId));

    // TODO: Add message to messages.yml
    player.sendMessage("Quest cancelled.");

    return true;

  }

}
