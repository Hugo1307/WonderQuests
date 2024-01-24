package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.concrete.ObtainPlayerActiveQuestsAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CurrentQuestsCommand extends AbstractPluginCommand {

  public CurrentQuestsCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    ActionsFactory actionFactory = dependencies.getActionsFactory();

    ObtainPlayerActiveQuestsAction obtainPlayerActiveQuestsAction = actionFactory.buildObtainPlayerActiveQuestsAction(
        sender);
    obtainPlayerActiveQuestsAction.execute().thenAccept((activeQuests) -> {

      if (activeQuests.isEmpty()) {
        // TODO: Add message to messages.yml
        sender.sendMessage("You don't have any active quests!");
        return;
      }

      sendQuestsToPlayer((Player) sender, activeQuests);

    });

    return true;

  }

  private void sendQuestsToPlayer(Player player, Set<ActiveQuestDto> activeQuests) {
    activeQuests.forEach((activeQuest) -> {

      double progressInPercentage = 100d;

      if (activeQuest.getTarget() != 0) {
        progressInPercentage = activeQuest.getProgress() * 100 / activeQuest.getTarget();
      }

      long timeLeft = (activeQuest.getQuestDetails().getTimeLimit()*1000 - (System.currentTimeMillis()
          - activeQuest.getStartedAt())) / 1000;

      player.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
      player.sendMessage(activeQuest.getQuestDetails().getName());
      player.sendMessage(activeQuest.getQuestDetails().getDescription());
      player.sendMessage("");
      player.sendMessage("Progress: " + progressInPercentage + " %");
      player.sendMessage("Time Left: " + (timeLeft > 0 ? timeLeft + "s" : "Expired"));
      player.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");

    });
  }

}
