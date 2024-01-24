package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.cache.implementation.ActiveQuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.PlayerDto;
import dev.hugog.minecraft.wonderquests.data.repositories.ActiveQuestRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestsRepository;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

}
