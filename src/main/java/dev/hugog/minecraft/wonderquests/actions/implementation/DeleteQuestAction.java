package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class represents the action of deleting a quest.
 */
public class DeleteQuestAction extends AbstractAction<CompletableFuture<Boolean>> {

  private final int questId;

  private final Messaging messaging;
  private final QuestsService questsService;

  @Inject
  public DeleteQuestAction(@Assisted CommandSender sender, @Assisted int questId,
      Messaging messaging, QuestsService questsService) {

    super(sender);
    this.questId = questId;
    this.messaging = messaging;
    this.questsService = questsService;

  }

  @Override
  public CompletableFuture<Boolean> execute() {

    if (!(sender instanceof Player player)) {
      return CompletableFuture.completedFuture(false);
    }

    return questsService.checkIfQuestExists(questId)
        .thenCompose(exists -> {
          if (!exists) {
            player.sendMessage(messaging.getLocalizedChatWithPrefix(
                "actions.quests.delete.not_found",
                Component.text(questId)
            ));
            return CompletableFuture.completedFuture(false);
          }

          return questsService.deleteQuest(questId)
              .thenApply(ignored -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.quests.delete.success",
                    Component.text(questId)
                ));
                return true;
              })
              .exceptionally(deleteThrowable -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.quests.delete.error",
                    Component.text(questId)
                ));
                return false;
              });
        })
        .exceptionally(throwable -> {
          player.sendMessage(messaging.getLocalizedChatWithPrefix(
              "actions.quests.delete.error",
              Component.text(questId)
          ));
          return false;
        });

  }

}
