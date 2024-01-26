package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.ShowQuestDetailsAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
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

    final Messaging messaging = dependencies.getMessaging();

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return false;
    }

    if (!player.hasPermission(CommandsPermissions.QUEST_DETAILS.getPermission())) {
      player.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return false;
    }

    if (args.length < 1 || !args[0].matches("\\d+")) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("commands.quest.details.usage"));
      return false;
    }

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    // Build and call the action
    ShowQuestDetailsAction action = actionsFactory.buildShowQuestDetailsAction(sender,
        Integer.parseInt(args[0]));
    return action.execute();

  }

}
