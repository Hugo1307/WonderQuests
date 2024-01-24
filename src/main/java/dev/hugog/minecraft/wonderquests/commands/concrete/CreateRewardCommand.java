package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestRewardAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;

public class CreateRewardCommand extends AbstractPluginCommand {

  public CreateRewardCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    ActionsFactory actionFactory = dependencies.getActionsFactory();

    CreateQuestRewardAction rewardCreateAction = actionFactory.buildCreateQuestRewardAction(sender,
        Integer.parseInt(args[0]));
    return rewardCreateAction.execute();

  }

}
