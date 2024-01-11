package dev.hugog.minecraft.wonderquests.commands;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import lombok.Getter;

/**
 * <h3>Command Dependencies</h3>
 * <h4>Represents the common dependencies of {@link PluginCommand} classes.</h4>
 * <br>
 * <p>It is used to inject dependencies that are used by all commands or at least by most of
 * them such as messaging and actions dependencies.</p>
 */
@Getter
public class CommandDependencies {

  private final Messaging messaging;
  private final ActionsFactory actionsFactory;

  @Inject
  private CommandDependencies(Messaging messaging, ActionsFactory actionsFactory) {
    this.messaging = messaging;
    this.actionsFactory = actionsFactory;
  }

}
