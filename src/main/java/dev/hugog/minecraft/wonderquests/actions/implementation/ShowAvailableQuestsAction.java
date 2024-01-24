package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.injection.factories.GuiFactory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowAvailableQuestsAction extends AbstractAction<Boolean> {

  private final GuiFactory guiFactory;

  @Inject
  public ShowAvailableQuestsAction(@Assisted CommandSender sender, GuiFactory guiFactory) {
    super(sender);
    this.guiFactory = guiFactory;
  }

  @Override
  public Boolean execute() {
    guiFactory.buildAvailableQuestsGui((Player) sender).open();
    return false;
  }

}
