package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.guis.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiClickListener implements Listener {

  private final GuiManager guiManager;

  @Inject
  public GuiClickListener(GuiManager guiManager) {
    this.guiManager = guiManager;
  }

  @EventHandler
  public void onGuiClick(InventoryClickEvent event) {

    Inventory inventory = event.getInventory();
    ItemStack clickedItem = event.getCurrentItem();

    // Clicked outside the inventory
    if (clickedItem == null) {
      return;
    }

    if (guiManager.isGuiRegistered(inventory)) {
      event.setCancelled(true);
      guiManager.getGuiByInventory(inventory).ifPresent(gui -> gui.onClick(clickedItem));
    }

  }

}
