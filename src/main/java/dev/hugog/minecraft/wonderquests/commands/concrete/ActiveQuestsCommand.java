package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.ShowActiveQuestsAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActiveQuestsCommand extends AbstractPluginCommand {

  public ActiveQuestsCommand(CommandSender sender, String[] args,
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

    if (player.hasPermission(CommandsPermissions.ACTIVE_QUESTS.getPermission())) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return false;
    }

    ActionsFactory actionFactory = dependencies.getActionsFactory();

    ShowActiveQuestsAction showActiveQuestsAction = actionFactory
        .buildShowActiveQuestsAction(sender);

    return showActiveQuestsAction.execute();

  }

}
