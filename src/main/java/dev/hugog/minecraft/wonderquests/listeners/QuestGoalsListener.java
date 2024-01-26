package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import dev.hugog.minecraft.wonderquests.mediators.QuestsMediator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class QuestGoalsListener implements Listener {

  private final ActiveQuestsService activeQuestsService;
  private final QuestsService questsService;
  private final QuestsMediator questsMediator;

  @Inject
  public QuestGoalsListener(ActiveQuestsService activeQuestsService, QuestsService questsService,
      QuestsMediator questsMediator) {
    this.activeQuestsService = activeQuestsService;
    this.questsService = questsService;
    this.questsMediator = questsMediator;
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

            // Even if the quest is expired, we still need to notify that the quest was updated.
            // This is because we need to trigger the ActiveQuestUpdateEvent, so we can remove
            // the quest if it is expired.
            questsMediator.notifyQuestUpdate(player, activeQuest);

            // If the quest is expired, we don't need to do anything with this event
            if (activeQuestsService.isQuestExpired(activeQuest.getPlayerId(),
                activeQuest.getQuestId())) {
              return;
            }

            questsService.getQuestById(activeQuest.getQuestId(), true).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();

              // If the broken block is the one we're looking for, we increase the progress
              if (objective.getType() == ObjectiveType.BREAK_BLOCK && objective.getStringValue()
                  .equals(brokenBlockMaterial.toString())) {

                questsMediator.updateQuestProgress(player, activeQuest);

                // Send a message to the player, using the action bar, with the quest progress
                sendProgressMessage(player, activeQuest);

                if (activeQuestsService.isQuestCompleted(activeQuest.getPlayerId(),
                    activeQuest.getQuestId())) {
                  questsMediator.handleQuestCompletion(player, activeQuest);
                }

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

            // Even if the quest is expired, we still need to notify that the quest was updated.
            // This is because we need to trigger the ActiveQuestUpdateEvent, so we can remove
            // the quest if it is expired.
            questsMediator.notifyQuestUpdate(player, activeQuest);

            // If the quest is expired, we don't need to do anything with this event
            if (activeQuestsService.isQuestExpired(activeQuest.getPlayerId(),
                activeQuest.getQuestId())) {
              return;
            }

            questsService.getQuestById(activeQuest.getQuestId(), true).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getStringValue().equals(block.getType().toString())
                  && objective.getType() == ObjectiveType.PLACE_BLOCK) {

                questsMediator.updateQuestProgress(player, activeQuest);

                // Send a message to the player, using the action bar, with the quest progress
                sendProgressMessage(player, activeQuest);

                if (activeQuestsService.isQuestCompleted(activeQuest.getPlayerId(),
                    activeQuest.getQuestId())) {
                  questsMediator.handleQuestCompletion(player, activeQuest);
                }

              }

            });

          });

        });

  }

  @EventHandler
  public void onMobKill(EntityDeathEvent event) {

    Player player = event.getEntity().getKiller();
    Entity killedEntity = event.getEntity();

    if (player == null) {
      return;
    }

    activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId())
        .thenAccept((activeQuests) -> {

          // The player doesn't have any active quests - no need to continue
          if (activeQuests.isEmpty()) {
            return;
          }

          activeQuests.forEach((activeQuest) -> {

            // Even if the quest is expired, we still need to notify that the quest was updated.
            // This is because we need to trigger the ActiveQuestUpdateEvent, so we can remove
            // the quest if it is expired.
            questsMediator.notifyQuestUpdate(player, activeQuest);

            // If the quest is expired, we don't need to do anything with this event
            if (activeQuestsService.isQuestExpired(activeQuest.getPlayerId(),
                activeQuest.getQuestId())) {
              return;
            }

            questsService.getQuestById(activeQuest.getQuestId(), true).thenAccept((quest) -> {

              // The quest doesn't exist
              if (quest.isEmpty()) {
                return;
              }

              QuestObjectiveDto objective = quest.get().getObjective();
              if (objective.getStringValue().equals(killedEntity.getType().toString())
                  && objective.getType() == ObjectiveType.KILL_MOBS) {

                questsMediator.updateQuestProgress(player, activeQuest);

                // Send a message to the player, using the action bar, with the quest progress
                sendProgressMessage(player, activeQuest);

                if (activeQuestsService.isQuestCompleted(activeQuest.getPlayerId(),
                    activeQuest.getQuestId())) {
                  questsMediator.handleQuestCompletion(player, activeQuest);
                }

              }

            });

          });

        });

  }

  private void sendProgressMessage(Player player, ActiveQuestDto activeQuest) {
    player.sendActionBar(
        Component.empty()
            .append(Component.text(
                activeQuest.getQuestDetails().getName(),
                NamedTextColor.BLUE)
            )
            .append(Component.text(" - ", NamedTextColor.GRAY))
            .append(Component.text(
                activeQuest.getProgress() + "/" + activeQuest.getTarget(),
                NamedTextColor.GRAY)
            )
    );
  }

}
