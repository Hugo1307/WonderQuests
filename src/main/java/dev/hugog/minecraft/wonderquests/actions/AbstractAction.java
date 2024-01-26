package dev.hugog.minecraft.wonderquests.actions;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;

/**
 * This class represents an abstract action.
 *
 * @param <T> The type of the result of the action.
 */
@AllArgsConstructor
public abstract class AbstractAction<T> implements Action<T> {

  protected CommandSender sender;

}
