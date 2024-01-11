package dev.hugog.minecraft.wonderquests.actions;

import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public abstract class AbstractAction implements Action {

  protected CommandSender sender;

}
