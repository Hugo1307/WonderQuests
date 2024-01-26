package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import dev.hugog.minecraft.wonderquests.mediators.SignsMediator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This class listens for updates to active quests and handles them accordingly.
 */
public class ActiveQuestUpdateListener implements Listener {

  private final SignsMediator signsMediator;
  private final ActiveQuestsService activeQuestsService;
  private final Messaging messaging;

  /**
   * Constructor for the ActiveQuestUpdateListener class.
   *
   * @param signsMediator The mediator for signs.
   * @param activeQuestsService The service for active quests.
   * @param messaging The messaging instance used for sending messages.
   */
  @Inject
  public ActiveQuestUpdateListener(SignsMediator signsMediator,
      ActiveQuestsService activeQuestsService, Messaging messaging) {
    this.signsMediator = signsMediator;
    this.activeQuestsService = activeQuestsService;
    this.messaging = messaging;
  }

  /**
   * This method handles the ActiveQuestUpdateEvent.
   *
   * @param event The ActiveQuestUpdateEvent to be handled.
   */
  @EventHandler
  public void onActiveQuestUpdate(ActiveQuestUpdateEvent event) {

    Player player = event.getPlayer();
    QuestUpdateType updateType = event.getUpdateType();
    ActiveQuestDto activeQuest = event.getActiveQuest();

    // Update the quests sign based on the type of update
    if (updateType == QuestUpdateType.UPDATED || updateType == QuestUpdateType.STARTED) {
      signsMediator.updateQuestsSign(player);
    } else {
      signsMediator.updateQuestsSignUsingActiveQuest(player, null);
    }

    // If the active quest is expired, remove it and send a message to the player
    if (activeQuest != null && activeQuest.isExpired()) {
      activeQuestsService.removeQuest(
              new PlayerQuestKey(player.getUniqueId(), activeQuest.getQuestDetails().getId())
          )
          .thenRun(() -> {
            player.sendMessage(messaging.getLocalizedChatWithPrefix(
                "general.quest.expired",
                Component.text(activeQuest.getQuestDetails().getName()))
            );
            signsMediator.updateQuestsSign(player);
          });
    } else {
      signsMediator.updateQuestsSign(player);
    }

  }

}