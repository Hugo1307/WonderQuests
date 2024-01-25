package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestRequirementAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateRequirementCommand extends AbstractPluginCommand {

  public CreateRequirementCommand(CommandSender sender, String[] args,
      CommandDependencies dependencies,
      Object... extraDependencies) {
    super(sender, args, dependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    final Messaging messaging = dependencies.getMessaging();

    if (!(sender instanceof Player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return false;
    }

    if (!sender.hasPermission(CommandsPermissions.CREATE_REQUIREMENT.getPermission())) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return false;
    }

    if (args.length < 1 || !args[0].matches("\\d+")) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("commands.requirement.create.usage"));
      return false;
    }

    ActionsFactory actionFactory = dependencies.getActionsFactory();

    CreateQuestRequirementAction requirementCreateAction = actionFactory.buildCreateQuestRequirementAction(
        sender,
        Integer.parseInt(args[0])
    );

    return requirementCreateAction.execute();

  }

}
