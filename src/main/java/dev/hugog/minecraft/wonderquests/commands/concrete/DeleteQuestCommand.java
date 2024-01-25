package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.actions.implementation.DeleteQuestAction;
import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteQuestCommand extends AbstractPluginCommand {

  public DeleteQuestCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    Messaging messaging = dependencies.getMessaging();

    if (!(sender instanceof Player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return false;
    }

    // TODO: Check permissions

    if (args.length < 1) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("commands.quest.delete.usage"));
      return false;
    }

    if (!args[0].matches("\\d+")) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("commands.quest.delete.usage"));
      return false;
    }

    int questId = Integer.parseInt(args[0]);

    ActionsFactory actionsFactory = dependencies.getActionsFactory();

    // Build and call the action
    DeleteQuestAction action = actionsFactory.buildDeleteQuestAction(sender, questId);
    action.execute();

    return true;

  }

}
