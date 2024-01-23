package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.services.PlayerService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;

public class QuestGoalsListener implements Listener {

  private final PlayerService playerService;
  private final QuestsService questsService;

  @Inject
  public QuestGoalsListener(PlayerService playerService, QuestsService questsService) {
    this.playerService = playerService;
    this.questsService = questsService;
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    Player player = event.getPlayer();

    playerService.getCurrentActiveQuests(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {
            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getType() == ObjectiveType.BREAK_BLOCK) {
                activeQuest.setProgress(activeQuest.getProgress() + 1);
              }

            });
          });
        });

  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {

    Player player = event.getPlayer();

    playerService.getCurrentActiveQuests(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {
            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getType() == ObjectiveType.PLACE_BLOCK) {
                activeQuest.setProgress(activeQuest.getProgress() + 1);
              }

            });
          });
        });

  }

  @EventHandler
  public void onItemCraft(CraftItemEvent event) {

    Player player = (Player) event.getWhoClicked();

    playerService.getCurrentActiveQuests(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {
            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getType() == ObjectiveType.CRAFT_ITEM) {
                activeQuest.setProgress(activeQuest.getProgress() + 1);
              }

            });
          });
        });

  }

  @EventHandler
  public void onMobKill(EntityDeathEvent event) {

    Player player = event.getEntity().getKiller();

    // If the player didn't kill the mob, we don't need to handle this event
    if (player == null) {
      return;
    }

    playerService.getCurrentActiveQuests(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {
            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getType() == ObjectiveType.KILL_MOBS) {
                activeQuest.setProgress(activeQuest.getProgress() + 1);
              }

            });
          });
        });

  }

  @EventHandler
  public void onPlayerKill(EntityDeathEvent event) {

    Player player = event.getEntity().getKiller();

    // If the player didn't kill the mob, we don't need to handle this event
    if (player == null) {
      return;
    }

    // If the player killed another entity that was not a player, we don't need to handle this event
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    playerService.getCurrentActiveQuests(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {
            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getType() == ObjectiveType.KILL_PLAYERS) {
                activeQuest.setProgress(activeQuest.getProgress() + 1);
              }

            });

          });

        });

  }

}