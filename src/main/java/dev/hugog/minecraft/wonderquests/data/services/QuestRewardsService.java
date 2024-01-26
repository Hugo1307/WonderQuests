package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * This class provides services for managing quest rewards in the game.
 */
public class QuestRewardsService {

  private final QuestRewardsRepository questRewardsRepository;

  /**
   * Constructor for the QuestRewardsService class.
   *
   * @param questRewardsRepository The repository instance used for database operations related to quest rewards.
   */
  @Inject
  public QuestRewardsService(QuestRewardsRepository questRewardsRepository) {
    this.questRewardsRepository = questRewardsRepository;
  }

  /**
   * This method deletes a quest reward.
   *
   * @param rewardId The id of the quest reward.
   * @return a CompletableFuture that will be completed when the quest reward is deleted.
   */
  public CompletableFuture<Void> deleteReward(Integer rewardId) {
    return questRewardsRepository.delete(rewardId);
  }

  /**
   * This method checks if a quest reward exists.
   *
   * @param rewardId The id of the quest reward.
   * @return a CompletableFuture that will be completed with a boolean indicating if the quest reward exists.
   */
  public CompletableFuture<Boolean> checkIfRewardExists(Integer rewardId) {
    return questRewardsRepository.findById(rewardId)
        .thenApply(Optional::isPresent);
  }

}