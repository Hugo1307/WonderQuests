package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.Action;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;

public class CurrentQuestsCommand extends AbstractPluginCommand {

  public CurrentQuestsCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    ActionsFactory actionFactory = dependencies.getActionsFactory();

    System.out.println("CurrentQuestsCommand.execute");

    Action checkCurrentQuestAction = actionFactory.buildCheckCurrentQuestAction(sender);
    return checkCurrentQuestAction.execute();

  }

}
