package dev.hugog.minecraft.wonderquests.commands;

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
  protected CommandDependencies dependencies;
  protected Object[] extraDependencies;

  public AbstractPluginCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies, Object... extraDependencies) {
    this.sender = sender;
    this.args = args;
    this.dependencies = commandDependencies;
    this.extraDependencies = extraDependencies;
  }

}
