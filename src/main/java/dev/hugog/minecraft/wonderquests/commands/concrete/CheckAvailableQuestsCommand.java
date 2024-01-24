package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.ShowAvailableQuestsAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckAvailableQuestsCommand extends AbstractPluginCommand {

  public CheckAvailableQuestsCommand(CommandSender sender, String[] args, CommandDependencies dependencies,
      Object... extraDependencies) {
    super(sender, args, dependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    if (!(sender instanceof Player)) {
      sender.sendMessage("Only players can create quests.");
      return false;
    }

    // TODO: Check permissions

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    // Build and call the action
    ShowAvailableQuestsAction action = actionsFactory.buildShowAvailableQuestsAction(sender);
    return action.execute();

  }

}
