package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.chat.messages.QuestStatusJoinMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowActiveQuestsAction extends AbstractAction<Boolean> {

  private final QuestStatusJoinMessage questStatusJoinMessage;

  @Inject
  public ShowActiveQuestsAction(@Assisted CommandSender sender,
      QuestStatusJoinMessage questStatusJoinMessage) {
    super(sender);
    this.questStatusJoinMessage = questStatusJoinMessage;
  }

  @Override
  public Boolean execute() {

    if (!(sender instanceof Player player)) {
      return false;
    }

    questStatusJoinMessage.displayActiveQuestsSummary(player);
    return true;

  }

}
