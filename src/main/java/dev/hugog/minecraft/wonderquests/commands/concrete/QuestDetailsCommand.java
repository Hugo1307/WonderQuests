package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowQuestDetailsAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import java.util.Arrays;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestDetailsCommand extends AbstractPluginCommand {

  public QuestDetailsCommand(CommandSender sender, String[] args,
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

    // TODO: Check arguments

    // Build and call the action
    ShowQuestDetailsAction action = actionsFactory.buildShowQuestDetailsAction(sender,
        Integer.parseInt(args[0]));
    return action.execute();

  }

}
