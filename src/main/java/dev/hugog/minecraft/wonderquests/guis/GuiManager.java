package dev.hugog.minecraft.wonderquests.guis;

import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.bukkit.inventory.Inventory;

@Singleton
@Getter
public class GuiManager {

  private final Set<Gui> registeredGuis;

  public GuiManager() {
    this.registeredGuis = new HashSet<>();
  }

  public void registerGui(Gui gui) {
    registeredGuis.add(gui);
  }

  public void unregisterGui(Gui gui) {
    registeredGuis.remove(gui);
  }

  public boolean isGuiRegistered(Inventory inventory) {
    return registeredGuis.stream()
        .anyMatch(gui -> gui.getInventory().equals(inventory));
  }

  public Optional<Gui> getGuiByInventory(Inventory inventory) {
    return registeredGuis.stream()
        .filter(gui -> gui.getInventory().equals(inventory))
        .findFirst();
  }

}
