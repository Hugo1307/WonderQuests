package dev.hugog.minecraft.wonderquests.guis;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.mediators.QuestsMediator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
  private final QuestsMediator questsMediator;
  private final GuiManager guiManager;
  private final ConcurrencyHandler concurrencyHandler;

  private final ActionsFactory actionsFactory;

  @Getter
  private Inventory inventory;

  @Inject
  public AvailableQuestsGui(@Assisted Player player, WonderQuests plugin, GuiManager guiManager,
      ConcurrencyHandler concurrencyHandler, QuestsMediator questsMediator,
      ActionsFactory actionsFactory) {

    this.player = player;
    this.plugin = plugin;
    this.guiManager = guiManager;
    this.concurrencyHandler = concurrencyHandler;
    this.questsMediator = questsMediator;
    this.actionsFactory = actionsFactory;

  }

  @Override
  public CompletableFuture<Void> build() {

    Component guiTitle = Component.text("Available Quests", NamedTextColor.DARK_GRAY);

    this.inventory = plugin.getServer().createInventory(player, 27, guiTitle);

    return questsMediator.getAvailableQuests(player).thenAccept((quests) -> {
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
    questItemMeta.displayName(Component.text(quest.getName())
        .color(NamedTextColor.GREEN)
        .decorate(TextDecoration.BOLD)
    );

    Component loreHeader = Component.empty()
        .append(Component.text(quest.getDescription(), NamedTextColor.GRAY));

    itemLore.add(loreHeader);
    itemLore.add(Component.empty());

    Component objectiveComponent = Component.empty()
        .append(Component.text("Objective:", NamedTextColor.GREEN))
        .appendSpace()
        .append(Component.text(quest.getObjective().obtainRepresentation(), NamedTextColor.GRAY));

    itemLore.add(objectiveComponent);
    itemLore.add(Component.empty());

    Component requirementsComponent = Component.text("Requirements", NamedTextColor.GREEN);

    itemLore.add(requirementsComponent);

    if (quest.getRequirements().isEmpty()) {
      itemLore.add(Component.text("None", NamedTextColor.GRAY));
    }

    for (QuestRequirementDto requirement : quest.getRequirements()) {

      Component individualRequirementComponent = Component.empty()
          .appendSpace()
          .appendSpace()
          .append(Component.text("• ", NamedTextColor.GREEN))
          .append(Component.text(requirement.obtainRepresentation(), NamedTextColor.GRAY));

      itemLore.add(individualRequirementComponent);

    }

    itemLore.add(Component.empty());

    Component rewardsComponent = Component.text("Rewards", NamedTextColor.GREEN);

    itemLore.add(rewardsComponent);

    if (quest.getRewards().isEmpty()) {
      itemLore.add(Component.text("None", NamedTextColor.GRAY));
    }

    for (QuestRewardDto reward : quest.getRewards()) {

      Component individualRewardComponent = Component.empty()
          .appendSpace()
          .appendSpace()
          .append(Component.text("• ", NamedTextColor.GREEN))
          .append(Component.text(reward.obtainRepresentation(), NamedTextColor.GRAY));

      itemLore.add(individualRewardComponent);

    }

    itemLore.add(Component.empty());

    questItemMeta.lore(itemLore);

    questItemMeta.getPersistentDataContainer()
        .set(getQuestIdKey(), PersistentDataType.INTEGER, quest.getId());

    questItem.setItemMeta(questItemMeta);

    return questItem;

  }

}
