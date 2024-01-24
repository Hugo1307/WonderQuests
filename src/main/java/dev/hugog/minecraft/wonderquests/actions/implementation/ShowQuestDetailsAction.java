package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.summaries.QuestDetailsSummary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowQuestDetailsAction extends AbstractAction<Boolean> {

  private final QuestDetailsSummary questDetailsSummary;

  public ShowQuestDetailsAction(@Assisted CommandSender sender, QuestDetailsSummary questDetailsSummary) {
    super(sender);
    this.questDetailsSummary = questDetailsSummary;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      return false;
    }

    questDetailsSummary.showToPlayer(player);

    return true;

  }

}
