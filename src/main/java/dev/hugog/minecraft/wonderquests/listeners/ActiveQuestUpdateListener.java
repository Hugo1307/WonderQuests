package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import dev.hugog.minecraft.wonderquests.mediators.SignsMediator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ActiveQuestUpdateListener implements Listener {

  private final SignsMediator signsMediator;

  @Inject
  public ActiveQuestUpdateListener(SignsMediator signsMediator) {
    this.signsMediator = signsMediator;
  }

  @EventHandler
  public void onActiveQuestUpdate(ActiveQuestUpdateEvent event) {

    Player player = event.getPlayer();
    QuestUpdateType updateType = event.getUpdateType();

    if (updateType == QuestUpdateType.UPDATED || updateType == QuestUpdateType.STARTED) {
      signsMediator.updateQuestsSign(player);
    } else {
      signsMediator.updateQuestsSignUsingActiveQuest(player, null);
    }

  }

}
