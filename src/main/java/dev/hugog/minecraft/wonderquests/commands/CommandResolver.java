package dev.hugog.minecraft.wonderquests.commands;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.commands.concrete.AbortQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CheckAvailableQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateRequirementCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateRewardCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.ActiveQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.DeleteQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.DeleteRequirementCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.DeleteRewardCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.HelpCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.ListQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.QuestDetailsCommand;
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

  protected PluginCommand pluginCommand;

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
      case "help" -> this.pluginCommand = new HelpCommand(sender, args, dependencies);
      case "create" -> this.pluginCommand = new CreateQuestCommand(sender, args, dependencies);
      case "details" -> this.pluginCommand = new QuestDetailsCommand(sender, args, dependencies);
      case "requirement" -> {

        if (args[0].equalsIgnoreCase("create")) {

          this.pluginCommand = new CreateRequirementCommand(sender,
              Arrays.copyOfRange(args, 1, args.length), dependencies);

        } else if (args[0].equalsIgnoreCase("delete")) {

          this.pluginCommand = new DeleteRequirementCommand(
              sender,
              Arrays.copyOfRange(args, 1, args.length),
              dependencies
          );

        }

      }
      case "reward" -> {
        if (args[0].equalsIgnoreCase("create")) {
          this.pluginCommand = new CreateRewardCommand(sender,
              Arrays.copyOfRange(args, 1, args.length), dependencies);
        } else if (args[0].equalsIgnoreCase("delete")) {
          this.pluginCommand = new DeleteRewardCommand(sender,
              Arrays.copyOfRange(args, 1, args.length), dependencies);
        }
      }
      case "abort" -> this.pluginCommand = new AbortQuestCommand(sender, args, dependencies);
      case "available" ->
          this.pluginCommand = new CheckAvailableQuestsCommand(sender, args, dependencies);
      case "active" -> this.pluginCommand = new ActiveQuestsCommand(sender, args, dependencies);
      case "list" -> this.pluginCommand = new ListQuestsCommand(sender, args, dependencies);
      case "delete" -> this.pluginCommand = new DeleteQuestCommand(sender, args, dependencies);
      default -> {
        this.pluginCommand = null;
        sender.sendMessage(
            dependencies.getMessaging().getLocalizedChatWithPrefix("general.unknown_command")
        );
      }
    }
  }

}
