package dev.hugog.minecraft.wonderquests.guis;

import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface Gui {

    CompletableFuture<Void> build();

    void open();

    void close();

    void onClick(ItemStack clickedItem);

    Inventory getInventory();

}
