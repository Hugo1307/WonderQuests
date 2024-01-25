package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.DeleteRewardAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteRewardCommand extends AbstractPluginCommand {

  public DeleteRewardCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    Messaging messaging = dependencies.getMessaging();

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return false;
    }

    if (!player.hasPermission(CommandsPermissions.DELETE_REWARD.getPermission())) {
      player.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return false;
    }

    if (args.length < 1 || !args[0].matches("\\d+")) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("commands.reward.delete.usage"));
      return false;
    }

    int rewardId = Integer.parseInt(args[0]);

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    // Build and call the action
    DeleteRewardAction action = actionsFactory.buildDeleteRewardAction(
        sender,
        rewardId
    );
    action.execute();

    return true;

  }

}
