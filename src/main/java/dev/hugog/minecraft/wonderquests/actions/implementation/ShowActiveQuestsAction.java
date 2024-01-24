package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.summaries.AvailableQuestsStatusSummary;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowActiveQuestsAction extends AbstractAction<Boolean> {

  private final AvailableQuestsStatusSummary availableQuestsStatusSummary;

  @Inject
  public ShowActiveQuestsAction(@Assisted CommandSender sender,
      AvailableQuestsStatusSummary availableQuestsStatusSummary) {
    super(sender);
    this.availableQuestsStatusSummary = availableQuestsStatusSummary;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      return false;
    }

    availableQuestsStatusSummary.showToPlayer(player);
    return true;

  }

}
