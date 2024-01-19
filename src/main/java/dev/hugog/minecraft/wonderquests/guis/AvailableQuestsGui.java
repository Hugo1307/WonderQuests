package dev.hugog.minecraft.wonderquests.guis;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    public Inventory build() {

        Inventory inventory = server.createInventory(player, 9, Component.text("Available Quests"));

        questsService.getAvailableQuests(player.getUniqueId())
            .thenAccept((quests) -> {
                quests.forEach((quest) -> inventory.addItem(buildItemFromQuest(quest)));
            });

        return inventory;

    }

    @Override
    public void open() {
        player.openInventory(build());
    }

    @Override
    public void close() {
        player.closeInventory();
    }

    private ItemStack buildItemFromQuest(QuestDto quest) {
        ItemStack questItem = new ItemStack(Material.EMERALD_BLOCK);
        List<Component> itemLore = new ArrayList<>();

        ItemMeta questItemMeta = questItem.getItemMeta();
        questItemMeta.displayName(Component.text(quest.getName()));

        itemLore.add(Component.text(quest.getDescription()));

        itemLore.add(Component.text(""));
        itemLore.add(Component.text("Requirements:"));
        itemLore.add(Component.text(""));
        quest.getRequirements().forEach((requirement) -> {
            itemLore.add(Component.text(requirement.obtainRepresentation()));
        });
        itemLore.add(Component.text(""));
        itemLore.add(Component.text("Rewards:"));
        itemLore.add(Component.text(""));
        quest.getRewards().forEach((reward) -> {
            itemLore.add(Component.text(reward.getType()));
        });
        itemLore.add(Component.text(""));
        itemLore.add(Component.text("Objectives:"));
        itemLore.add(Component.text(""));
        quest.getObjectives().forEach((objective) -> {
            itemLore.add(Component.text(objective.getType()));
        });
        itemLore.add(Component.text(""));
        questItemMeta.lore(itemLore);
        questItem.setItemMeta(questItemMeta);

        return questItem;

    }

}
