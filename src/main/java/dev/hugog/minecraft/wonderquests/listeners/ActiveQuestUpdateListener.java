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

public class ActiveQuestUpdateListener implements Listener {

  private final SignsMediator signsMediator;
  private final ActiveQuestsService activeQuestsService;
  private final Messaging messaging;

  @Inject
  public ActiveQuestUpdateListener(SignsMediator signsMediator,
      ActiveQuestsService activeQuestsService, Messaging messaging) {
    this.signsMediator = signsMediator;
    this.activeQuestsService = activeQuestsService;
    this.messaging = messaging;
  }

  @EventHandler
  public void onActiveQuestUpdate(ActiveQuestUpdateEvent event) {

    Player player = event.getPlayer();
    QuestUpdateType updateType = event.getUpdateType();
    ActiveQuestDto activeQuest = event.getActiveQuest();

    if (updateType == QuestUpdateType.UPDATED || updateType == QuestUpdateType.STARTED) {
      signsMediator.updateQuestsSign(player);
    } else {
      signsMediator.updateQuestsSignUsingActiveQuest(player, null);
    }

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
