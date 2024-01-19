package dev.hugog.minecraft.wonderquests.injection.factories;

import dev.hugog.minecraft.wonderquests.guis.AvailableQuestsGui;
import org.bukkit.entity.Player;

public interface GuiFactory {

  AvailableQuestsGui buildAvailableQuestsGui(Player player);

}
