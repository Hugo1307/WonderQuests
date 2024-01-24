package dev.hugog.minecraft.wonderquests.mediators;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

public class QuestRewardsMediator {

  private final Logger logger;
  private final ConcurrencyHandler concurrencyHandler;
  private final QuestsService questsService;
  private final WonderQuests plugin;

  @Inject
  public QuestRewardsMediator(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, QuestsService questsService,
      WonderQuests plugin) {
    this.logger = logger;
    this.concurrencyHandler = concurrencyHandler;
    this.questsService = questsService;
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
          case COMMAND -> {
            concurrencyHandler.runOnMainThread(
                () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    reward.getStringValue().replace("%player%", player.getName())
                ));
          }
        }
      });

    });

  }

}
