package dev.hugog.minecraft.wonderquests.commands;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateQuestCommand;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.interaction.InteractiveSessionManager;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;

/**
 * <h3>Command Invoker</h3>
 * <h4>Invokes a {@link PluginCommand},i.e., a concrete command.</h4>
 * <br>
 * <p>The {@link CommandInvoker} is part of the <strong>Commander Pattern</strong> and is used to
 * invoke a {@link PluginCommand} based on the command label.</p>
 *
 * <p>Depending on the command, this class can also inject the necessary command's
 * dependencies.</p>
 */
public class CommandInvoker {

  private PluginCommand pluginCommand;

  private final Messaging messaging;
  private final InteractiveSessionManager interactiveSessionManager;
  private final QuestsService questsService;

  @Inject
  public CommandInvoker(Messaging messaging, InteractiveSessionManager interactiveSessionManager, QuestsService questsService) {
    this.messaging = messaging;
    this.interactiveSessionManager = interactiveSessionManager;
    this.questsService = questsService;
  }

  public boolean executeCommand() {
    if (pluginCommand == null) {
      return false;
    }
    return pluginCommand.execute();
  }

  /**
   * Sets the command to be executed using the command label.
   *
   * <p>You should register your commands here using their label.</p>
   *
   * @param commandLabel the command string, i.e., command name.
   */
  public void setPluginCommand(String commandLabel, CommandSender sender, String[] args) {
    if (commandLabel.equals("create")) {
      this.pluginCommand = new CreateQuestCommand(sender, args, messaging, interactiveSessionManager, questsService);
    } else {
      this.pluginCommand = null;
    }
  }

}
