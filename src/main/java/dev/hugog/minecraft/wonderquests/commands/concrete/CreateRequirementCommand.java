package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.concrete.CreateQuestRequirementAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.command.CommandSender;

public class CreateRequirementCommand extends AbstractPluginCommand {

  public CreateRequirementCommand(CommandSender sender, String[] args,
      CommandDependencies dependencies,
      Object... extraDependencies) {
    super(sender, args, dependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    ActionsFactory actionFactory = dependencies.getActionsFactory();

    CreateQuestRequirementAction requirementCreateAction = actionFactory.buildCreateQuestRequirementAction(sender,
        Integer.parseInt(args[0]));
    return requirementCreateAction.execute();

  }

}
