package dev.hugog.minecraft.wonderquests.commands;

import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * <h3>Bukkit Command Executor</h3>
 * <h4>Represents the default Bukkit command executor.</h4>
 * <br>
 * <p>The class receives commands from the chat and redirects them to the {@link CommandResolver} if
 * they are commands from this plugin.</p>
 *
 * <p>The {@link CommandResolver}, part of the <strong>Commander Pattern</strong>, will create a new
 * {@link PluginCommand} instance based on the command's label and execute the command.</p>
 *
 * <p>See {@link CommandResolver#setPluginCommand(String, CommandSender, String[])}.</p>
 */
public class BukkitCommandExecutor implements CommandExecutor {

  private final CommandResolver commandResolver;

  @Inject
  public BukkitCommandExecutor(CommandResolver commandResolver) {
    this.commandResolver = commandResolver;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {

    if (!command.getName().equalsIgnoreCase("quests")) {
      return false;
    }

    if (args.length == 0) {
      commandSender.sendMessage("§cUsage: /quests <create>");
      return true;
    }

    commandResolver.setPluginCommand(args[0], commandSender, args);
    return commandResolver.executeCommand();

  }

}
