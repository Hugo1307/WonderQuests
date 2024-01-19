package dev.hugog.minecraft.wonderquests.actions.concrete;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.injection.factories.GuiFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowAvailableQuestsAction extends AbstractAction {

  private final GuiFactory guiFactory;

  @Inject
  public ShowAvailableQuestsAction(@Assisted CommandSender sender, GuiFactory guiFactory) {
    super(sender);
    this.guiFactory = guiFactory;
  }

  @Override
  public boolean execute() {
    guiFactory.buildAvailableQuestsGui((Player) sender).open();
    return false;
  }

}