package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.ShowActiveQuestsAction;
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

    ShowActiveQuestsAction showActiveQuestsAction = actionFactory
        .buildShowActiveQuestsAction(sender);

    return showActiveQuestsAction.execute();

  }

}
