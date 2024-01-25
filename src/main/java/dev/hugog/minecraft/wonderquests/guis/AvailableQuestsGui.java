package dev.hugog.minecraft.wonderquests.guis;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AvailableQuestsGui implements Gui {

  private final Player player;
  private final WonderQuests plugin;
  private final QuestsService questsService;
  private final ActiveQuestsService activeQuestsService;
  private final GuiManager guiManager;
  private final ConcurrencyHandler concurrencyHandler;

  @Getter
  private Inventory inventory;

  @Inject
  public AvailableQuestsGui(@Assisted Player player, WonderQuests plugin,
      ActiveQuestsService activeQuestsService, GuiManager guiManager,
      ConcurrencyHandler concurrencyHandler, QuestsService questsService) {
    this.player = player;
    this.plugin = plugin;
    this.activeQuestsService = activeQuestsService;
    this.guiManager = guiManager;
    this.concurrencyHandler = concurrencyHandler;
    this.questsService = questsService;
  }

  @Override
  public CompletableFuture<Void> build() {

    this.inventory = plugin.getServer()
        .createInventory(player, 9, Component.text("Available Quests"));

    return questsService.getAvailableQuests(player.getUniqueId()).thenAccept((quests) -> {
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
    player.closeInventory();
  }

  @Override
  public void onClick(ItemStack clickedItem) {

    PersistentDataContainer itemPersistentContainer = clickedItem.getItemMeta()
        .getPersistentDataContainer();

    if (itemPersistentContainer.has(getQuestIdKey(), PersistentDataType.INTEGER)) {

      Integer questId = itemPersistentContainer.get(getQuestIdKey(), PersistentDataType.INTEGER);

      activeQuestsService.hasAlreadyStartedQuest(player.getUniqueId(), questId)
          .thenAccept((alreadyStarted) -> {

            if (alreadyStarted) {
              player.sendMessage("You have already started this quest!");
              return;
            }

            questsService.getQuestById(questId).thenAccept((quest) -> {
              if (quest.isEmpty()) {
                player.sendMessage("Quest not found!");
                return;
              }

              activeQuestsService.startQuest(player.getUniqueId(), questId,
                      quest.get().getObjective().getNumericValue())
                  .whenComplete((success, throwable) -> {

                    if (throwable != null) {
                      player.sendMessage("An error occurred while starting the quest!");
                      return;
                    }

                    if (success) {

                      concurrencyHandler.run(() -> plugin.getServer().getPluginManager()
                              .callEvent(new ActiveQuestUpdateEvent(player, QuestUpdateType.STARTED)),
                          true);

                      player.sendMessage("Quest started!");

                    } else {
                      player.sendMessage("Quest could not be started!");
                    }

                  });

            });


          });

      close();

    }

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
