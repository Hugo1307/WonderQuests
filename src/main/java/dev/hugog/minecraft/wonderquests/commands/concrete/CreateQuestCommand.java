package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.concrete.CreateQuestAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * <h3>Create Quest Command</h3>
 * <h4>Represents the command to create a new quest.</h4>
 *
 * <p>The command will be executed as follows:</p>
 * /quests create
 */
public class CreateQuestCommand extends AbstractPluginCommand {

  public CreateQuestCommand(CommandSender sender, String[] args, CommandDependencies dependencies,
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
    CreateQuestAction action = actionsFactory.buildCreateQuestAction(sender);
    return action.execute();

  }

}
