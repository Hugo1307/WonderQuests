package dev.hugog.minecraft.wonderquests.actions;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public abstract class AbstractAction<T> implements Action<T> {

  protected CommandSender sender;

}
