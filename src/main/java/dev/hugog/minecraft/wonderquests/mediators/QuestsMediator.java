package dev.hugog.minecraft.wonderquests.mediators;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

public class QuestsMediator {

  private final Logger logger;
  private final ConcurrencyHandler concurrencyHandler;
  private final QuestsService questsService;
  private final ActiveQuestsService activeQuestsService;
  private final WonderQuests plugin;

  @Inject
  public QuestsMediator(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, QuestsService questsService,
      ActiveQuestsService activeQuestsService,
      WonderQuests plugin) {
    this.logger = logger;
    this.concurrencyHandler = concurrencyHandler;
    this.questsService = questsService;
    this.activeQuestsService = activeQuestsService;
    this.plugin = plugin;
  }

  public void giveQuestRewardsToPlayer(Player player, Integer questId) {

    // TODO: Add messages to the player when the rewards are granted
    questsService.getQuestById(questId).thenAccept(questOptional -> {

      if (questOptional.isEmpty()) {
        logger.info(
            "Unable to grant rewards to player " + player.getName() + " for quest " + questId
                + " - quest not found.");
        return;
      }

      QuestDto quest = questOptional.get();

      quest.getRewards().forEach(reward -> {

        switch (reward.getType()) {
          case EXPERIENCE -> player.giveExp(reward.getNumericValue().intValue());
          case MONEY, ITEMS -> {
          }
          case COMMAND -> concurrencyHandler.runOnMainThread(
              () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                  reward.getStringValue().replace("%player%", player.getName())
              ));
        }
      });

    });

  }

  public void updateQuestProgress(Player player, ActiveQuestDto activeQuest) {

    // Call the ActiveQuestUpdateEvent to update the signs, for example.
    concurrencyHandler.run(
        () -> plugin.getServer().getPluginManager().callEvent(
            new ActiveQuestUpdateEvent(player, QuestUpdateType.UPDATED)
        ),
        true
    );

    activeQuestsService.incrementQuestProgress(player.getUniqueId(), activeQuest.getQuestId());

  }

  public void handleQuestCompletion(Player player, ActiveQuestDto activeQuest) {

    activeQuestsService.removeQuest(
        new PlayerQuestKey(player.getUniqueId(), activeQuest.getQuestId())).thenRun(() -> {

      giveQuestRewardsToPlayer(player, activeQuest.getQuestId());

      questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

        // The quest doesn't exist
        if (quest.isEmpty()) {
          return;
        }

        concurrencyHandler.run(() -> plugin.getServer().getPluginManager()
            .callEvent(new ActiveQuestUpdateEvent(player, QuestUpdateType.COMPLETED)), true);

        QuestDto questDto = quest.get();

        player.sendMessage("Quest completed!");
        player.showTitle(Title.title(Component.text("Quest completed!"),
            Component.text(questDto.getName() + " completed!")));
      });

    });

  }

}
