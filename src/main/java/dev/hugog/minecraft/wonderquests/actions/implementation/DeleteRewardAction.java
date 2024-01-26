package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.services.QuestRewardsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class represents the action of deleting a quest reward.
 */
public class DeleteRewardAction extends AbstractAction<CompletableFuture<Boolean>> {

  private final int rewardId;
  private final Messaging messaging;
  private final QuestRewardsService questRewardsService;

  @Inject
  public DeleteRewardAction(@Assisted CommandSender sender, @Assisted int rewardId,
      Messaging messaging,
      QuestRewardsService questRewardsService) {

    super(sender);
    this.rewardId = rewardId;
    this.messaging = messaging;
    this.questRewardsService = questRewardsService;

  }

  @Override
  public CompletableFuture<Boolean> execute() {

    if (!(sender instanceof Player player)) {
      return CompletableFuture.completedFuture(false);
    }

    return questRewardsService.checkIfRewardExists(rewardId)
        .thenCompose(exists -> {

          if (!exists) {
            player.sendMessage(messaging.getLocalizedChatWithPrefix(
                "actions.rewards.delete.not_found",
                Component.text(rewardId)
            ));
            return CompletableFuture.completedFuture(false);
          }

          return questRewardsService.deleteReward(rewardId)
              .thenApply(ignored -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.rewards.delete.success",
                    Component.text(rewardId)
                ));
                return true;
              })
              .exceptionally(deleteThrowable -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.rewards.delete.error",
                    Component.text(rewardId)
                ));
                return false;
              });

        })
        .exceptionally(throwable -> {
          player.sendMessage(messaging.getLocalizedChatWithPrefix(
              "actions.rewards.delete.error",
              Component.text(rewardId)
          ));
          return false;
        });

  }

}
