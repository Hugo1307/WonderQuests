package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import dev.hugog.minecraft.wonderquests.language.PluginMessage;
import org.bukkit.command.CommandSender;

public class CreateQuestCommand extends AbstractPluginCommand {

  /**
   * <h3>Create Quest Command</h3>
   * <h4>Represents the command to create a new quest.</h4>
   *
   * <p>The command will be executed as follows:</p>
   * /quests create <name> <description>
   *
   * @param sender The command sender
   * @param args The command arguments
   * @param messaging The messaging service
   * @param dependencies Any extra dependencies
   */
  public CreateQuestCommand(CommandSender sender, String[] args, Messaging messaging,
      Object... dependencies) {
    super(sender, args, messaging, dependencies);
  }

  @Override
  public boolean execute() {

    if (args.length < 2) {
      sender.sendMessage(messaging.getLocalMessage(PluginMessage.QUEST_CREATION_CMD_USAGE));
      return false;
    }

    return true;

  }

}
