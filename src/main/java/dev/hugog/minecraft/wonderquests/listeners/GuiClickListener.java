package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.guis.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * This class listens for GUI click events and handles them accordingly.
 */
public class GuiClickListener implements Listener {

  private final GuiManager guiManager;

  /**
   * Constructor for the GuiClickListener class.
   *
   * @param guiManager The manager for GUIs.
   */
  @Inject
  public GuiClickListener(GuiManager guiManager) {
    this.guiManager = guiManager;
  }

  /**
   * This method handles the InventoryClickEvent.
   *
   * @param event The InventoryClickEvent to be handled.
   */
  @EventHandler
  public void onGuiClick(InventoryClickEvent event) {

    Inventory inventory = event.getInventory();
    ItemStack clickedItem = event.getCurrentItem();

    // If the clicked item is null, the player clicked outside the inventory
    if (clickedItem == null) {
      return;
    }

    // If the clicked inventory is a registered GUI, cancel the event and handle the click
    if (guiManager.isGuiRegistered(inventory)) {
      event.setCancelled(true);
      guiManager.getGuiByInventory(inventory).ifPresent(gui -> gui.onClick(clickedItem));
    }

  }

}