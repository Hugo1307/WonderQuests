package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.summaries.QuestDetailsSummary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowQuestDetailsAction extends AbstractAction<Boolean> {

  private final int questId;
  private final QuestDetailsSummary questDetailsSummary;

  @Inject
  public ShowQuestDetailsAction(@Assisted CommandSender sender, @Assisted int questId, QuestDetailsSummary questDetailsSummary) {
    super(sender);
    this.questId = questId;
    this.questDetailsSummary = questDetailsSummary;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      return false;
    }

    questDetailsSummary.showToPlayer(player, questId);

    return true;

  }

}
