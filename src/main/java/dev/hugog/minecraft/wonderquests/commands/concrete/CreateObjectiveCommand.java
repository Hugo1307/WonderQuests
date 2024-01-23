package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.concrete.CreateQuestObjectiveAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateObjectiveCommand extends AbstractPluginCommand {

  public CreateObjectiveCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
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
    CreateQuestObjectiveAction action = actionsFactory.buildCreateQuestObjectiveAction(sender,
        Integer.parseInt(args[0]));
    return action.execute();

  }

}
