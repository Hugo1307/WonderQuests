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

public class CompletedQuestsService {

  private final CompletedQuestRepository completedQuestRepository;

  @Inject
  public CompletedQuestsService(CompletedQuestRepository completedQuestRepository) {
    this.completedQuestRepository = completedQuestRepository;
  }

  public CompletableFuture<Boolean> addCompletedQuest(CompletedQuestDto completedQuestDto) {
    return completedQuestRepository.insert(completedQuestDto.toModel())
        .thenApply(Objects::nonNull);
  }

  public CompletableFuture<Set<CompletedQuestDto>> getCompletedQuestByPlayer(UUID playerId) {
    return completedQuestRepository.findAllByPlayer(playerId)
        .thenApply(completedQuestModels -> completedQuestModels.stream()
            .map(CompletedQuestModel::toDto)
            .collect(Collectors.toSet()));
  }

}
