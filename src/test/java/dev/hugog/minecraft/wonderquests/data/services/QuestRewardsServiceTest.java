package dev.hugog.minecraft.wonderquests.data.services;

import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import dev.hugog.minecraft.wonderquests.data.types.RewardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuestRewardsServiceTest {

  @Mock
  private QuestRewardsRepository questRewardsRepository;

  @InjectMocks
  private QuestRewardsService questRewardsService;

  private QuestRewardModel questRewardModel;
  private Integer rewardId;

  @BeforeEach
  public void setUp() {
    rewardId = 1;
    questRewardModel = new QuestRewardModel(rewardId, 1, RewardType.MONEY.name(), null, 2f);
  }

  @Test
  @DisplayName("Delete reward is successful if is also successful in database layer")
  public void deleteRewardSuccessfully() {

    when(questRewardsRepository.delete(rewardId))
        .thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<Void> result = questRewardsService.deleteReward(rewardId);

    assertNull(result.join());

  }

  @Test
  @DisplayName("Check if reward exists returns true")
  public void checkIfRewardExistsReturnsTrue() {

    when(questRewardsRepository.findById(rewardId))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(questRewardModel)));

    CompletableFuture<Boolean> result = questRewardsService.checkIfRewardExists(rewardId);

    assertTrue(result.join());

  }

  @Test
  @DisplayName("Check if reward exists returns false")
  public void checkIfRewardExistsReturnsFalse() {

    when(questRewardsRepository.findById(any(Integer.class)))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    CompletableFuture<Boolean> result = questRewardsService.checkIfRewardExists(rewardId);

    assertFalse(result.join());

  }

}