package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.ShowAvailableQuestsAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CheckAvailableQuestsCommand extends AbstractPluginCommand {

  public CheckAvailableQuestsCommand(CommandSender sender, String[] args, CommandDependencies dependencies,
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

    if (!sender.hasPermission(CommandsPermissions.AVAILABLE_QUESTS.getPermission())) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return false;
    }

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    // Build and call the action
    ShowAvailableQuestsAction action = actionsFactory.buildShowAvailableQuestsAction(sender);
    return action.execute();

  }

}
