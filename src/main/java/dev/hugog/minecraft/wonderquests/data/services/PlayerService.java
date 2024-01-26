package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.PlayerDto;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerService {

  private final PlayersRepository playersRepository;

  @Inject
  public PlayerService(PlayersRepository playersRepository) {
    this.playersRepository = playersRepository;
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
