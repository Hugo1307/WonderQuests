package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class represents the action of cancelling a quest.
 */
public class CancelQuestAction extends AbstractAction<CompletableFuture<Boolean>> {

  private final int questId;

  private final Messaging messaging;
  private final ActiveQuestsService activeQuestsService;
  private final WonderQuests plugin;
  private final ConcurrencyHandler concurrencyHandler;

  @Inject
  public CancelQuestAction(@Assisted CommandSender sender, @Assisted int questId,
      Messaging messaging,
      ActiveQuestsService activeQuestsService, WonderQuests plugin,
      ConcurrencyHandler concurrencyHandler) {

    super(sender);
    this.questId = questId;
    this.messaging = messaging;
    this.activeQuestsService = activeQuestsService;
    this.plugin = plugin;
    this.concurrencyHandler = concurrencyHandler;

  }

  @Override
  public CompletableFuture<Boolean> execute() {

    if (!(sender instanceof Player player)) {
      sender.sendMessage(messaging.getLocalizedChatWithPrefix("general.players_only"));
      return CompletableFuture.completedFuture(false);
    }

    // Send pending message to provide feedback to the player
    player.sendMessage(messaging.getLocalizedChatWithPrefix("actions.quest.abort.pending"));

    return activeQuestsService.hasAlreadyStartedQuest(player.getUniqueId(), questId)
        .thenCompose(hasStarted -> {

          if (!hasStarted) {
            player.sendMessage(messaging.getLocalizedChatWithPrefix("actions.quests.abort.not_found"));
            return CompletableFuture.completedFuture(false);
          }

          return activeQuestsService.removeQuest(new PlayerQuestKey(player.getUniqueId(), questId))
              .thenApply(ignored -> {

                // Call the event to notify that the quest has been cancelled
                concurrencyHandler.run(() -> plugin.getServer().getPluginManager()
                        .callEvent(new ActiveQuestUpdateEvent(player, QuestUpdateType.CANCELLED, null)),
                    true);

                player.sendMessage(
                    messaging.getLocalizedChatWithPrefix("actions.quests.abort.success")
                );

                return true;

              })
              .exceptionally(throwable -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix("actions.quests.abort.error"));
                return false;
              });


        })
        .exceptionally(throwable -> {
          player.sendMessage(messaging.getLocalizedChatWithPrefix("actions.quests.abort.error"));
          return false;
        });

  }

}
