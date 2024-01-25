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
        .append(messaging.getLocalizedRawMessage("commands.help.title")
            .color(NamedTextColor.GREEN)
            .decorate(TextDecoration.BOLD)
        )
        .appendNewline()
        .appendNewline()
        .append(commandHelpComponent("quests",
            messaging.getLocalizedRawMessage("commands.help.help.command")))
        .appendNewline()
        .append(commandHelpComponent("quests available",
            messaging.getLocalizedRawMessage("commands.help.quest.available.command")))
        .appendNewline()
        .append(commandHelpComponent("quests active",
            messaging.getLocalizedRawMessage("commands.help.quest.active.command")))
        .appendNewline()
        .append(commandHelpComponent("quests abort <quest_id>",
            messaging.getLocalizedRawMessage("commands.help.quest.abort.command")))
        .appendNewline()
        .append(commandHelpComponent("quests details <quest_id>",
            messaging.getLocalizedRawMessage("commands.help.quest.details.command")))
        .appendNewline()
        .append(commandHelpComponent("quests create",
            messaging.getLocalizedRawMessage("commands.help.quest.create.command")))
        .appendNewline()
        .append(commandHelpComponent("quests delete <quest_id>",
            messaging.getLocalizedRawMessage("commands.help.quest.delete.command")))
        .appendNewline()
        .append(commandHelpComponent("quests list <page>",
            messaging.getLocalizedRawMessage("commands.help.quest.list.command")))
        .appendNewline()
        .append(commandHelpComponent("quests reward create <quest_id>",
            messaging.getLocalizedRawMessage("commands.help.quest.reward.create.command")))
        .appendNewline()
        .append(commandHelpComponent("quests reward delete <id>",
            messaging.getLocalizedRawMessage("commands.help.quest.reward.delete.command")))
        .appendNewline()
        .append(commandHelpComponent("quests requirement create <quest_id>",
            messaging.getLocalizedRawMessage("commands.help.quest.requirement.create.command")))
        .appendNewline()
        .append(commandHelpComponent("quests requirement delete <id>",
            messaging.getLocalizedRawMessage("commands.help.quest.requirement.delete.command")));

    sender.sendMessage(helpMessage);
    sender.sendMessage(messaging.getChatSeparator());

    return true;

  }

  private Component commandHelpComponent(String command, Component description) {
    return Component.text("  /" + command, NamedTextColor.GREEN)
        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command))
        .hoverEvent(Component.text("Click to run this command", NamedTextColor.GRAY))
        .appendNewline()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .appendSpace()
        .append(Component.text("• ", NamedTextColor.GRAY))
        .append(description.color(NamedTextColor.GRAY))
        .appendNewline();
  }


}
