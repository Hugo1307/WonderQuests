package dev.hugog.minecraft.wonderquests.commands;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.commands.concrete.CheckAvailableQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateRequirementCommand;
import java.util.Arrays;
import org.bukkit.command.CommandSender;

/**
 * <h3>Command Invoker</h3>
 * <h4>Invokes a {@link PluginCommand},i.e., a concrete command.</h4>
 * <br>
 * <p>The {@link CommandResolver} is part of the <strong>Commander Pattern</strong> and is used to
 * invoke a {@link PluginCommand} based on the command label.</p>
 *
 * <p>Depending on the command, this class can also inject the necessary command's
 * dependencies.</p>
 */
public class CommandResolver {

  private PluginCommand pluginCommand;

  private final CommandDependencies dependencies;

  @Inject
  public CommandResolver(CommandDependencies dependencies) {
    this.dependencies = dependencies;
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
    switch (commandLabel) {
      case "create" -> this.pluginCommand = new CreateQuestCommand(sender, args, dependencies);
      case "requirement" -> {
        if (args[0].equalsIgnoreCase("create")) {
          this.pluginCommand = new CreateRequirementCommand(sender,
              Arrays.copyOfRange(args, 1, args.length), dependencies);
        }
      }
      case "available" ->
          this.pluginCommand = new CheckAvailableQuestsCommand(sender, args, dependencies);
      default -> this.pluginCommand = null;
    }
  }

}
