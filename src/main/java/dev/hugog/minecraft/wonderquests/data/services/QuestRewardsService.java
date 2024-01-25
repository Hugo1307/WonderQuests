package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class QuestRewardsService {

  private final QuestRewardsRepository questRewardsRepository;

  @Inject
  public QuestRewardsService(QuestRewardsRepository questRewardsRepository) {
    this.questRewardsRepository = questRewardsRepository;
  }

  public CompletableFuture<Void> deleteReward(Integer rewardId) {
    return questRewardsRepository.delete(rewardId);
  }

  public CompletableFuture<Boolean> checkIfRewardExists(Integer rewardId) {
    return questRewardsRepository.findById(rewardId)
        .thenApply(Optional::isPresent);
  }

}
