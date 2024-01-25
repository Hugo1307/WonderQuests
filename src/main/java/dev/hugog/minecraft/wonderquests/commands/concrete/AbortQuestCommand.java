package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.CancelQuestAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AbortQuestCommand extends AbstractPluginCommand {

  public AbortQuestCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    final Messaging messaging = dependencies.getMessaging();

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return false;
    }

    if (player.hasPermission(CommandsPermissions.ABORT_QUEST.getPermission())) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return false;
    }

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    if (args.length < 1 || !args[0].matches("\\d+")) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("commands.quest.abort.usage"));
      return false;
    }

    // Build and call the action
    CancelQuestAction action = actionsFactory.buildCancelQuestAction(sender,
        Integer.parseInt(args[0]));
    action.execute();

    return true;

  }

}
