package dev.hugog.minecraft.wonderquests.guis;

import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import java.util.List;

public class AvailableQuestsGui implements Gui {

    private final Server server;
    private final Player player;

    private final QuestsService questsService;

    @Inject
    public AvailableQuestsGui(@Assisted Player player, QuestsService questsService, Server server) {
        this.player = player;
        this.questsService = questsService;
        this.server = server;
    }

    @Override
    public void build() {

        // List<QuestDto> availableQuests = questsService.getAvailableQuests(player);

        Inventory inventory = server.createInventory(player, 9, Component.text("Available Quests"));

        inventory.setItem(0, new ItemStack(Material.EMERALD_BLOCK));


    }

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }

}
