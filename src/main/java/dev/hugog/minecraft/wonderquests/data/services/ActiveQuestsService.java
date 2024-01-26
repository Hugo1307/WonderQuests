package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.cache.implementation.ActiveQuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import dev.hugog.minecraft.wonderquests.data.repositories.ActiveQuestRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class provides services for managing active quests in the game.
 */
public class ActiveQuestsService {

  private final ActiveQuestsCache activeQuestsCache;
  private final ActiveQuestRepository activeQuestRepository;

  /**
   * Constructor for the ActiveQuestsService class.
   *
   * @param activeQuestsCache      The cache instance used for caching active quests.
   * @param activeQuestRepository  The repository instance used for database operations related to active quests.
   */
  @Inject
  public ActiveQuestsService(ActiveQuestsCache activeQuestsCache,
      ActiveQuestRepository activeQuestRepository) {
    this.activeQuestsCache = activeQuestsCache;
    this.activeQuestRepository = activeQuestRepository;
  }

  /**
   * This method starts a quest for a player.
   *
   * @param playerId         The id of the player.
   * @param questId          The id of the quest.
   * @param objectiveTarget  The target objective of the quest.
   * @return a CompletableFuture that will be completed with a boolean indicating if the quest was started successfully.
   */
  public CompletableFuture<Boolean> startQuest(UUID playerId, Integer questId,
      Float objectiveTarget) {

    ActiveQuestDto activeQuestDto = ActiveQuestDto.startQuest(playerId, questId, objectiveTarget);
    return activeQuestRepository.insert(activeQuestDto.toModel()).thenApply(Objects::nonNull);

  }

  /**
   * This method checks if a player has already started a quest.
   *
   * @param playerId The id of the player.
   * @param questId  The id of the quest.
   * @return a CompletableFuture that will be completed with a boolean indicating if the player has already started the quest.
   */
  public CompletableFuture<Boolean> hasAlreadyStartedQuest(UUID playerId, Integer questId) {

    PlayerQuestKey playerQuestKey = new PlayerQuestKey(playerId, questId);
    return activeQuestRepository.findById(playerQuestKey).thenApply(Optional::isPresent);

  }

  /**
   * This method gets all active quests for a player.
   *
   * @param playerId The id of the player.
   * @return a CompletableFuture that will be completed with a Set containing all active quests for the player.
   */
  public CompletableFuture<Set<ActiveQuestDto>> getActiveQuestsForPlayer(UUID playerId) {

    if (activeQuestsCache.has(playerId)) {
      return CompletableFuture.completedFuture(activeQuestsCache.get(playerId));
    }

    return activeQuestRepository.findAllByPlayerId(playerId)
        .thenApply(questModels -> {
              Set<ActiveQuestDto> activeQuests = questModels.stream()
                  .map(ActiveQuestModel::toDto)
                  .collect(Collectors.toSet());

              activeQuestsCache.put(playerId, activeQuests);

              return activeQuests;
            }
        );

  }

  /**
   * This method removes a quest from the active quests of a player.
   *
   * @param activeQuestKey The key of the active quest.
   * @return a CompletableFuture that will be completed when the quest is removed.
   */
  public CompletableFuture<Void> removeQuest(PlayerQuestKey activeQuestKey) {

    if (activeQuestsCache.has(activeQuestKey)) {
      activeQuestsCache.invalidate(activeQuestKey);
    }

    return activeQuestRepository.delete(activeQuestKey);

  }

  /**
   * This method checks if a quest is completed.
   *
   * @param playerId The id of the player.
   * @param questId  The id of the quest.
   * @return a boolean indicating if the quest is completed.
   */
  public boolean isQuestCompleted(UUID playerId, Integer questId) {

    return activeQuestsCache.get(playerId).stream()
        .filter(activeQuestDto -> activeQuestDto.getQuestId().equals(questId))
        .findFirst()
        .map(activeQuestDto -> activeQuestDto.getProgress() >= activeQuestDto.getTarget())
        .orElse(false);
  }

  /**
   * This method checks if a quest is expired.
   *
   * @param playerId The id of the player.
   * @param questId  The id of the quest.
   * @return a boolean indicating if the quest is expired.
   */
  public boolean isQuestExpired(UUID playerId, Integer questId) {
    return activeQuestsCache.get(playerId).stream()
        .filter(activeQuestDto -> activeQuestDto.getQuestId().equals(questId))
        .findFirst()
        .map(activeQuestDto ->
            activeQuestDto.getQuestDetails().getTimeLimit() * 1000 -
                (System.currentTimeMillis() - activeQuestDto.getStartedAt()) < 0)
        .orElse(false);
  }

  /**
   * This method increments the progress of a quest.
   *
   * @param playerId The id of the player.
   * @param questId  The id of the quest.
   */
  public void incrementQuestProgress(UUID playerId, Integer questId) {

    getActiveQuestsForPlayer(playerId)
        .thenAccept((activeQuests) -> {
          activeQuests.stream()
              .filter(activeQuestDto -> activeQuestDto.getQuestId().equals(questId))
              .findFirst()
              .ifPresent(
                  activeQuestDto -> activeQuestDto.setProgress(activeQuestDto.getProgress() + 1));
        });

  }

  /**
   * This method saves an active quest.
   *
   * @param activeQuestDto The DTO of the active quest.
   * @return a CompletableFuture that will be completed with a boolean indicating if the active quest was saved successfully.
   */
  public CompletableFuture<Boolean> saveActiveQuest(ActiveQuestDto activeQuestDto) {

    ActiveQuestModel activeQuestModel = activeQuestDto.toModel();
    return activeQuestRepository.save(activeQuestModel);

  }

}