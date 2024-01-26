package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.summaries.AdminQuestDetailsSummary;
import dev.hugog.minecraft.wonderquests.chat.summaries.QuestDetailsSummary;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class represents the action of showing the details of a quest to a player.
 */
public class ShowQuestDetailsAction extends AbstractAction<Boolean> {

  private final int questId;
  private final QuestDetailsSummary questDetailsSummary;
  private final AdminQuestDetailsSummary adminQuestDetailsSummary;

  @Inject
  public ShowQuestDetailsAction(@Assisted CommandSender sender, @Assisted int questId,
      QuestDetailsSummary questDetailsSummary, AdminQuestDetailsSummary adminQuestDetailsSummary) {
    super(sender);
    this.questId = questId;
    this.questDetailsSummary = questDetailsSummary;
    this.adminQuestDetailsSummary = adminQuestDetailsSummary;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      return false;
    }

    if (player.hasPermission(CommandsPermissions.ADMIN.getPermission())) {
      adminQuestDetailsSummary.showToPlayer(player, questId);
    } else {
      questDetailsSummary.showToPlayer(player, questId);
    }

    return true;

  }

}
