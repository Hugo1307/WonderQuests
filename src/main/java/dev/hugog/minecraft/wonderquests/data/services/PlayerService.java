package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.cache.ActiveQuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.PlayerDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.repositories.ActiveQuestRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestsRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerService {

  private final PlayersRepository playersRepository;
  private final QuestsRepository questsRepository;
  private final ActiveQuestRepository activeQuestRepository;

  private final ActiveQuestsCache activeQuestsCache;

  @Inject
  public PlayerService(PlayersRepository playersRepository, QuestsRepository questsRepository,
      ActiveQuestRepository activeQuestRepository, ActiveQuestsCache activeQuestsCache) {
    this.playersRepository = playersRepository;
    this.questsRepository = questsRepository;
    this.activeQuestRepository = activeQuestRepository;
    this.activeQuestsCache = activeQuestsCache;
  }

  public CompletableFuture<Boolean> checkPlayer(UUID uuid) {
    return playersRepository.findById(uuid).thenCompose(playerModel -> {
      if (playerModel.isEmpty()) {
        return createPlayer(uuid);
      }
      return CompletableFuture.completedFuture(true);
    });
  }

  private CompletableFuture<Boolean> createPlayer(UUID uuid) {
    PlayerDto playerDto = new PlayerDto(uuid);
    return playersRepository.insert(playerDto.toModel()).thenApply(Objects::nonNull);
  }

  public CompletableFuture<Set<QuestDto>> getAvailableQuests(UUID playerId) {
    return questsRepository.findAll().thenApply(
        questModels -> questModels.stream()
            .map(QuestModel::toDto)
            .collect(Collectors.toSet())
    );
  }

  public CompletableFuture<Boolean> startQuest(UUID playerId, Integer questId, Float objectiveTarget) {

    ActiveQuestDto activeQuestDto = ActiveQuestDto.startQuest(playerId, questId, objectiveTarget);
    return activeQuestRepository.insert(activeQuestDto.toModel()).thenApply(Objects::nonNull);

  }

  public CompletableFuture<Boolean> alreadyStartedQuest(UUID playerId, Integer questId) {

    PlayerQuestKey playerQuestKey = new PlayerQuestKey(playerId, questId);
    return activeQuestRepository.findById(playerQuestKey).thenApply(Optional::isPresent);

  }

  public CompletableFuture<Set<ActiveQuestDto>> getCurrentActiveQuests(UUID playerId) {

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

    UUID playerId = activeQuestDto.getPlayerId();
    if (activeQuestsCache.has(playerId)) {
      activeQuestsCache.invalidate(playerId);
    }
    return activeQuestRepository.delete(new PlayerQuestKey(activeQuestDto.getPlayerId(), activeQuestDto.getQuestId()));

  }

  public boolean isQuestCompleted(ActiveQuestDto activeQuest) {
    return activeQuest.getProgress() >= activeQuest.getTarget();
  }

}
