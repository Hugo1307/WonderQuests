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
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ActiveQuestsService {

  private final ActiveQuestsCache activeQuestsCache;
  private final ActiveQuestRepository activeQuestRepository;

  private final ReentrantLock lock = new ReentrantLock();

  @Inject
  public ActiveQuestsService(ActiveQuestsCache activeQuestsCache,
      ActiveQuestRepository activeQuestRepository) {
    this.activeQuestsCache = activeQuestsCache;
    this.activeQuestRepository = activeQuestRepository;
  }

  public CompletableFuture<Boolean> startQuest(UUID playerId, Integer questId,
      Float objectiveTarget) {

    ActiveQuestDto activeQuestDto = ActiveQuestDto.startQuest(playerId, questId, objectiveTarget);
    return activeQuestRepository.insert(activeQuestDto.toModel()).thenApply(Objects::nonNull);

  }

  public CompletableFuture<Boolean> hasAlreadyStartedQuest(UUID playerId, Integer questId) {

    PlayerQuestKey playerQuestKey = new PlayerQuestKey(playerId, questId);
    return activeQuestRepository.findById(playerQuestKey).thenApply(Optional::isPresent);

  }

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

  public CompletableFuture<Void> completeQuest(ActiveQuestDto activeQuestDto) {

    PlayerQuestKey playerQuestKey = new PlayerQuestKey(activeQuestDto.getPlayerId(), activeQuestDto.getQuestId());

    if (activeQuestsCache.has(playerQuestKey)) {
      activeQuestsCache.invalidate(playerQuestKey);
    }

    return activeQuestRepository.delete(
        new PlayerQuestKey(activeQuestDto.getPlayerId(), activeQuestDto.getQuestId()));

  }

  public boolean isQuestCompleted(UUID playerId, Integer questId) {
    return activeQuestsCache.get(playerId).stream()
        .filter(activeQuestDto -> activeQuestDto.getQuestId().equals(questId))
        .findFirst()
        .map(activeQuestDto -> activeQuestDto.getProgress() >= activeQuestDto.getTarget())
        .orElse(false);
  }

  public boolean isQuestExpired(UUID playerId, Integer questId) {
    return activeQuestsCache.get(playerId).stream()
        .filter(activeQuestDto -> activeQuestDto.getQuestId().equals(questId))
        .findFirst()
        .map(activeQuestDto ->
            activeQuestDto.getQuestDetails().getTimeLimit() * 1000 -
                (System.currentTimeMillis() - activeQuestDto.getStartedAt()) < 0)
        .orElse(false);
  }

  public void incrementQuestProgress(UUID playerId, Integer questId) {

    getActiveQuestsForPlayer(playerId)
        .thenAccept((activeQuests) -> {
          activeQuests.stream()
              .filter(activeQuestDto -> activeQuestDto.getQuestId().equals(questId))
              .findFirst()
              .ifPresent(activeQuestDto -> {
                System.out.println("Incrementing quest progress...");
                System.out.println("New progress: " + (activeQuestDto.getProgress() + 1));
                activeQuestDto.setProgress(activeQuestDto.getProgress() + 1);
              });
        });

  }

  public CompletableFuture<Boolean> saveActiveQuest(ActiveQuestDto activeQuestDto) {
    ActiveQuestModel activeQuestModel = activeQuestDto.toModel();
    System.out.println("Saving active quest: " + activeQuestModel);
    return activeQuestRepository.save(activeQuestModel).thenApply(Objects::nonNull);
  }

}
