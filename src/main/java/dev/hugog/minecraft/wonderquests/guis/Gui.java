package dev.hugog.minecraft.wonderquests.guis;

import org.bukkit.inventory.Inventory;

public interface Gui {

    Inventory build();

    void open();

    void close();


}
