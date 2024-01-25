package dev.hugog.minecraft.wonderquests.guis;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class AvailableQuestsGui implements Gui {

  private final Player player;
  private final WonderQuests plugin;
  private final QuestsService questsService;
  private final GuiManager guiManager;
  private final ConcurrencyHandler concurrencyHandler;

  private final ActionsFactory actionsFactory;

  @Getter
  private Inventory inventory;

  @Inject
  public AvailableQuestsGui(@Assisted Player player, WonderQuests plugin, GuiManager guiManager,
      ConcurrencyHandler concurrencyHandler, QuestsService questsService,
      ActionsFactory actionsFactory) {

    this.player = player;
    this.plugin = plugin;
    this.guiManager = guiManager;
    this.concurrencyHandler = concurrencyHandler;
    this.questsService = questsService;
    this.actionsFactory = actionsFactory;

  }

  @Override
  public CompletableFuture<Void> build() {

    Component guiTitle = Component.text("Available Quests", NamedTextColor.DARK_GRAY);

    this.inventory = plugin.getServer().createInventory(player, 27, guiTitle);

    return questsService.getAvailableQuests(player).thenAccept((quests) -> {
      quests.forEach((quest) -> inventory.addItem(buildItemFromQuest(quest)));
    });

  }

  @Override
  public void open() {
    build().thenRun(
        () -> concurrencyHandler.runOnMainThread(() -> player.openInventory(inventory)));
    guiManager.registerGui(this);
  }

  @Override
  public void close() {
    guiManager.unregisterGui(this);
    concurrencyHandler.runOnMainThread(player::closeInventory);
  }

  @Override
  public void onClick(ItemStack clickedItem) {

    actionsFactory.buildAcceptQuestAction(player, clickedItem).execute()
        .thenAccept(accepted -> {
          if (accepted) {
            close();
          }
        });

  }

  private NamespacedKey getQuestIdKey() {
    return new NamespacedKey(plugin, "quest_id");
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
      itemLore.add(Component.text(reward.obtainRepresentation()));
    });
    itemLore.add(Component.text(""));
    itemLore.add(Component.text("Objective:"));
    itemLore.add(Component.text(""));
    itemLore.add(Component.text(quest.getObjective().obtainRepresentation()));

    itemLore.add(Component.text(""));

    questItemMeta.lore(itemLore);

    questItemMeta.getPersistentDataContainer()
        .set(getQuestIdKey(), PersistentDataType.INTEGER, quest.getId());

    questItem.setItemMeta(questItemMeta);

    return questItem;

  }

}
