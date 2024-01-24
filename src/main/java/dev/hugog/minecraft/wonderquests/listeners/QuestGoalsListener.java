package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import dev.hugog.minecraft.wonderquests.mediators.QuestRewardsMediator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class QuestGoalsListener implements Listener {

  private final ActiveQuestsService activeQuestsService;
  private final QuestsService questsService;
  private final QuestRewardsMediator questRewardsMediator;

  @Inject
  public QuestGoalsListener(ActiveQuestsService activeQuestsService, QuestsService questsService,
      QuestRewardsMediator questRewardsMediator) {
    this.activeQuestsService = activeQuestsService;
    this.questsService = questsService;
    this.questRewardsMediator = questRewardsMediator;
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    Player player = event.getPlayer();
    Block block = event.getBlock();

    Material brokenBlockMaterial = block.getType();

    activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {

            // If the quest is expired, we don't need to do anything with this event
            if (activeQuestsService.isQuestExpired(activeQuest.getPlayerId(),
                activeQuest.getQuestId())) {
              return;
            }

            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();

              // If the broken block is the one we're looking for, we increase the progress
              if (objective.getType() == ObjectiveType.BREAK_BLOCK && objective.getStringValue()
                  .equals(brokenBlockMaterial.toString())) {

                if (activeQuestsService.isQuestCompleted(activeQuest.getPlayerId(),
                    activeQuest.getQuestId())) {
                  handleQuestCompletion(player, activeQuest);
                  return;
                }

                activeQuestsService.incrementQuestProgress(activeQuest.getPlayerId(),
                    activeQuest.getQuestId());
                // TODO: Improve this message
                player.sendMessage("+1");

              }

            });

          });

        });

  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {

    Player player = event.getPlayer();
    Block block = event.getBlock();

    activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {

            // If the quest is expired, we don't need to do anything with this event
            if (activeQuestsService.isQuestExpired(activeQuest.getPlayerId(),
                activeQuest.getQuestId())) {
              return;
            }

            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getStringValue().equals(block.getType().toString())
                  && objective.getType() == ObjectiveType.PLACE_BLOCK) {

                if (activeQuestsService.isQuestCompleted(activeQuest.getPlayerId(),
                    activeQuest.getQuestId())) {
                  handleQuestCompletion(player, activeQuest);
                  return;
                }

                activeQuestsService.incrementQuestProgress(activeQuest.getPlayerId(),
                    activeQuest.getQuestId());
                // TODO: Improve this message
                player.sendMessage("+1");

              }

            });

          });

        });

  }

//  @EventHandler
//  public void onItemCraft(CraftItemEvent event) {
//
//    Player player = (Player) event.getWhoClicked();
//
//    playerService.getCurrentActiveQuests(player.getUniqueId())
//        .thenAccept((activeQuests) -> {
//
//          // The player doesn't have any active quests - no need to continue
//          if (activeQuests.isEmpty()) {
//            return;
//          }
//
//          activeQuests.forEach((activeQuest) -> {
//            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {
//
//              // The quest doesn't exist
//              if (quest.isEmpty()) {
//                return;
//              }
//
//              QuestObjectiveDto objective = quest.get().getObjective();
//              if (objective.getType() == ObjectiveType.CRAFT_ITEM) {
//                activeQuest.setProgress(activeQuest.getProgress() + 1);
//              }
//
//            });
//          });
//        });
//
//  }
//
//  @EventHandler
//  public void onMobKill(EntityDeathEvent event) {
//
//    Player player = event.getEntity().getKiller();
//
//    // If the player didn't kill the mob, we don't need to handle this event
//    if (player == null) {
//      return;
//    }
//
//    playerService.getCurrentActiveQuests(player.getUniqueId())
//        .thenAccept((activeQuests) -> {
//
//          // The player doesn't have any active quests - no need to continue
//          if (activeQuests.isEmpty()) {
//            return;
//          }
//
//          activeQuests.forEach((activeQuest) -> {
//            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {
//
//              // The quest doesn't exist
//              if (quest.isEmpty()) {
//                return;
//              }
//
//              QuestObjectiveDto objective = quest.get().getObjective();
//              if (objective.getType() == ObjectiveType.KILL_MOBS) {
//                activeQuest.setProgress(activeQuest.getProgress() + 1);
//              }
//
//            });
//          });
//        });
//
//  }
//
//  @EventHandler
//  public void onPlayerKill(EntityDeathEvent event) {
//
//    Player player = event.getEntity().getKiller();
//
//    // If the player didn't kill the mob, we don't need to handle this event
//    if (player == null) {
//      return;
//    }
//
//    // If the player killed another entity that was not a player, we don't need to handle this event
//    if (!(event.getEntity() instanceof Player)) {
//      return;
//    }
//
//    playerService.getCurrentActiveQuests(player.getUniqueId())
//        .thenAccept((activeQuests) -> {
//
//          // The player doesn't have any active quests - no need to continue
//          if (activeQuests.isEmpty()) {
//            return;
//          }
//
//          activeQuests.forEach((activeQuest) -> {
//            questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {
//
//              // The quest doesn't exist
//              if (quest.isEmpty()) {
//                return;
//              }
//
//              QuestObjectiveDto objective = quest.get().getObjective();
//              if (objective.getType() == ObjectiveType.KILL_PLAYERS) {
//                activeQuest.setProgress(activeQuest.getProgress() + 1);
//              }
//
//            });
//
//          });
//
//        });
//
//  }

  private void handleQuestCompletion(Player player, ActiveQuestDto activeQuest) {

    activeQuestsService.removeQuest(
        new PlayerQuestKey(player.getUniqueId(), activeQuest.getQuestId())).thenRun(() -> {

      questRewardsMediator.giveQuestRewardsToPlayer(player, activeQuest.getQuestId());

      questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

        // The quest doesn't exist
        if (quest.isEmpty()) {
          return;
        }

        QuestDto questDto = quest.get();

        player.sendMessage("Quest completed!");
        player.showTitle(Title.title(Component.text("Quest completed!"),
            Component.text(questDto.getName() + " completed!")));
      });

    });

  }

}
