package dev.hugog.minecraft.wonderquests.commands;

import dev.hugog.minecraft.wonderquests.language.Messaging;
import org.bukkit.command.CommandSender;

/**
 * <h3>Abstract Plugin Command</h3>
 * <h4>Represents an Abstract Command in the Commander Pattern</h4>
 * <br>
 * <p>Plugin commands are used to execute commands from the plugin.</p>
 * <p>It is part of the commander pattern that is being implemented.</p>
 */
public abstract class AbstractPluginCommand implements PluginCommand {

  protected CommandSender sender;
  protected String[] args;
  protected Messaging messaging;
  protected Object[] dependencies;

  public AbstractPluginCommand(CommandSender sender, String[] args, Messaging messaging,
      Object... dependencies) {
    this.sender = sender;
    this.args = args;
    this.messaging = messaging;
    this.dependencies = dependencies;
  }

}
