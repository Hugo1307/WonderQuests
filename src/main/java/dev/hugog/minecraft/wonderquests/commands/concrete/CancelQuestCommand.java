package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.CancelQuestAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelQuestCommand extends AbstractPluginCommand {

  public CancelQuestCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    if (!(sender instanceof Player)) {
      // TODO: Add message to messages.yml
      sender.sendMessage("Only players can create quests.");
      return false;
    }

    // TODO: Check permissions

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    // TODO: Check arguments

    // Build and call the action
    CancelQuestAction action = actionsFactory.buildCancelQuestAction(sender, Integer.parseInt(args[0]));
    action.execute();

    return true;

  }

}
