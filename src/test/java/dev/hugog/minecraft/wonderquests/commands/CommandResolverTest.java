package dev.hugog.minecraft.wonderquests.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.commands.concrete.AbortQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.ActiveQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CheckAvailableQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateRequirementCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.CreateRewardCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.DeleteQuestCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.DeleteRequirementCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.DeleteRewardCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.HelpCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.ListQuestsCommand;
import dev.hugog.minecraft.wonderquests.commands.concrete.QuestDetailsCommand;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandResolverTest {

  @Mock
  private CommandSender sender;
  @Mock
  private CommandDependencies dependencies;

  private CommandResolver commandResolver;

  @BeforeEach
  void setUp() {
    commandResolver = new CommandResolver(dependencies);
  }

  @Test
  @DisplayName("executeCommand() returns false when no command is set")
  public void executeCommand_ReturnsFalseWhenNoCommandIsSet() {

    assertFalse(commandResolver.executeCommand());

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'help'")
  public void setPluginCommand_SetsCorrectCommandForHelp() {

    commandResolver.setPluginCommand("help", sender, new String[]{});
    assertInstanceOf(HelpCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'create'")
  public void setPluginCommand_SetsCorrectCommandForCreate() {

    commandResolver.setPluginCommand("create", sender, new String[]{});
    assertInstanceOf(CreateQuestCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'details'")
  public void setPluginCommand_SetsCorrectCommandForDetails() {

    commandResolver.setPluginCommand("details", sender, new String[]{});
    assertInstanceOf(QuestDetailsCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'requirement create'")
  public void setPluginCommand_SetsCorrectCommandForRequirementCreate() {

    commandResolver.setPluginCommand("requirement", sender, new String[]{"create"});
    assertInstanceOf(CreateRequirementCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'requirement delete'")
  public void setPluginCommand_SetsCorrectCommandForRequirementDelete() {

    commandResolver.setPluginCommand("requirement", sender, new String[]{"delete"});
    assertInstanceOf(DeleteRequirementCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'reward create'")
  public void setPluginCommand_SetsCorrectCommandForRewardCreate() {

    commandResolver.setPluginCommand("reward", sender, new String[]{"create"});
    assertInstanceOf(CreateRewardCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'reward delete'")
  public void setPluginCommand_SetsCorrectCommandForRewardDelete() {

    commandResolver.setPluginCommand("reward", sender, new String[]{"delete"});
    assertInstanceOf(DeleteRewardCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'abort'")
  public void setPluginCommand_SetsCorrectCommandForAbort() {

    commandResolver.setPluginCommand("abort", sender, new String[]{});
    assertInstanceOf(AbortQuestCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'available'")
  public void setPluginCommand_SetsCorrectCommandForAvailable() {

    commandResolver.setPluginCommand("available", sender, new String[]{});
    assertInstanceOf(CheckAvailableQuestsCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'active'")
  public void setPluginCommand_SetsCorrectCommandForActive() {

    commandResolver.setPluginCommand("active", sender, new String[]{});
    assertInstanceOf(ActiveQuestsCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'list'")
  public void setPluginCommand_SetsCorrectCommandForList() {

    commandResolver.setPluginCommand("list", sender, new String[]{});
    assertInstanceOf(ListQuestsCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets the correct command for 'delete'")
  public void setPluginCommand_SetsCorrectCommandForDelete() {

    commandResolver.setPluginCommand("delete", sender, new String[]{});
    assertInstanceOf(DeleteQuestCommand.class, commandResolver.pluginCommand);

  }

  @Test
  @DisplayName("setPluginCommand() sets no command for unknown command")
  public void setPluginCommand_SetsNoCommandForUnknownCommand() {

    Messaging messaging = mock(Messaging.class);

    when(dependencies.getMessaging()).thenReturn(messaging);
    when(messaging.getLocalizedChatWithPrefix("general.unknown_command"))
        .thenReturn(Component.text("Unknown command."));

    commandResolver.setPluginCommand("unknown", sender, new String[]{});
    assertFalse(commandResolver.executeCommand());
    verify(sender).sendMessage(Component.text("Unknown command."));

  }

}