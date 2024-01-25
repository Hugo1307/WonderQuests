package dev.hugog.minecraft.wonderquests.commands.concrete;

import dev.hugog.minecraft.wonderquests.commands.AbstractPluginCommand;
import dev.hugog.minecraft.wonderquests.commands.CommandDependencies;
import dev.hugog.minecraft.wonderquests.commands.CommandsPermissions;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand extends AbstractPluginCommand {

  public HelpCommand(CommandSender sender, String[] args,
      CommandDependencies commandDependencies,
      Object... extraDependencies) {
    super(sender, args, commandDependencies, extraDependencies);
  }

  @Override
  public boolean execute() {

    final Messaging messaging = dependencies.getMessaging();

    if (sender instanceof Player player && !player.hasPermission(
        CommandsPermissions.HELP.getPermission())) {

      player.sendMessage(messaging.getLocalizedChatWithPrefix("general.no_permission"));
      return true;

    }

    sender.sendMessage(messaging.getChatSeparator());

    Component helpMessage = Component.empty()
        .appendNewline()
        .append(Component.text("WonderQuests Help", NamedTextColor.GREEN)
            .decorate(TextDecoration.BOLD)
        )
        .appendNewline()
        .appendNewline()
        .append(commandHelpComponent("quests", "Displays this help menu"))
        .appendNewline()
        .append(commandHelpComponent("quests available", "Lists all available quests"))
        .appendNewline()
        .append(commandHelpComponent("quests active", "Lists all your active quests"))
        .appendNewline()
        .append(commandHelpComponent("quests abort <quest_id>", "Abort a started quest"))
        .appendNewline()
        .append(commandHelpComponent("quests details <quest_id>", "Displays details for a quest"))
        .appendNewline()
        .append(commandHelpComponent("quests create", "Create a new quest"))
        .appendNewline()
        .append(commandHelpComponent("quests delete <quest_id>", "Delete a quest"))
        .appendNewline()
        .append(commandHelpComponent("quests list <page>", "Lists all quests"))
        .appendNewline()
        .append(commandHelpComponent("quests reward create <quest_id>", "Create a new reward"))
        .appendNewline()
        .append(commandHelpComponent("quests reward delete <id>", "Delete a reward"))
        .appendNewline()
        .append(commandHelpComponent("quests requirement create <quest_id>", "Create a new requirement"))
        .appendNewline()
        .append(commandHelpComponent("quests requirement delete <id>", "Delete a requirement."));

    sender.sendMessage(helpMessage);
    sender.sendMessage(messaging.getChatSeparator());

    return true;

  }

  private Component commandHelpComponent(String command, String description) {
    return Component.text("  /" + command, NamedTextColor.GREEN)
        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command))
        .hoverEvent(Component.text("Click to run this command", NamedTextColor.GRAY))
        .appendNewline()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .append(Component.text("• ", NamedTextColor.GRAY))
        .append(Component.text(description, NamedTextColor.GRAY))
        .appendNewline();
  }


}
