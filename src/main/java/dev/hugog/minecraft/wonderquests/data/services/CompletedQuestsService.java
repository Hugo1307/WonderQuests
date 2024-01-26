package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.CompletedQuestDto;
import dev.hugog.minecraft.wonderquests.data.models.CompletedQuestModel;
import dev.hugog.minecraft.wonderquests.data.repositories.CompletedQuestRepository;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class provides services for managing completed quests in the game.
 */
public class CompletedQuestsService {

  private final CompletedQuestRepository completedQuestRepository;

  /**
   * Constructor for the CompletedQuestsService class.
   *
   * @param completedQuestRepository The repository instance used for database operations related to completed quests.
   */
  @Inject
  public CompletedQuestsService(CompletedQuestRepository completedQuestRepository) {
    this.completedQuestRepository = completedQuestRepository;
  }

  /**
   * This method adds a completed quest for a player.
   *
   * @param completedQuestDto The DTO of the completed quest.
   * @return a CompletableFuture that will be completed with a boolean indicating if the quest was added successfully.
   */
  public CompletableFuture<Boolean> addCompletedQuest(CompletedQuestDto completedQuestDto) {
    return completedQuestRepository.insert(completedQuestDto.toModel())
        .thenApply(Objects::nonNull);
  }

  /**
   * This method gets all completed quests for a player.
   *
   * @param playerId The id of the player.
   * @return a CompletableFuture that will be completed with a Set containing all completed quests for the player.
   */
  public CompletableFuture<Set<CompletedQuestDto>> getCompletedQuestByPlayer(UUID playerId) {
    return completedQuestRepository.findAllByPlayer(playerId)
        .thenApply(completedQuestModels -> completedQuestModels.stream()
            .map(CompletedQuestModel::toDto)
            .collect(Collectors.toSet()));
  }

  /**
   * This method checks if a player has completed a specific quest.
   *
   * @param playerId The id of the player.
   * @param questId  The id of the quest.
   * @return a CompletableFuture that will be completed with a boolean indicating if the player has completed the quest.
   */
  public CompletableFuture<Boolean> hasCompletedQuest(UUID playerId, Integer questId) {
    return getCompletedQuestByPlayer(playerId)
        .thenApply(completedQuests -> completedQuests.stream()
            .anyMatch(completedQuest -> completedQuest.getQuestId().equals(questId)));
  }

}