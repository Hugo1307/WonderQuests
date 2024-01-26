package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.PlayerDto;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class provides services for managing players in the game.
 */
public class PlayerService {

  private final PlayersRepository playersRepository;

  /**
   * Constructor for the PlayerService class.
   *
   * @param playersRepository The repository instance used for database operations related to players.
   */
  @Inject
  public PlayerService(PlayersRepository playersRepository) {
    this.playersRepository = playersRepository;
  }

  /**
   * This method checks if a player exists in the database.
   * If the player does not exist, it creates a new player.
   *
   * @param uuid The UUID of the player.
   * @return a CompletableFuture that will be completed with a boolean indicating if the player exists or was created successfully.
   */
  public CompletableFuture<Boolean> checkPlayer(UUID uuid) {
    return playersRepository.findById(uuid).thenCompose(playerModel -> {
      if (playerModel.isEmpty()) {
        return createPlayer(uuid);
      }
      return CompletableFuture.completedFuture(true);
    });
  }

  /**
   * This method creates a new player in the database.
   *
   * @param uuid The UUID of the player.
   * @return a CompletableFuture that will be completed with a boolean indicating if the player was created successfully.
   */
  public CompletableFuture<Boolean> createPlayer(UUID uuid) {
    PlayerDto playerDto = new PlayerDto(uuid);
    return playersRepository.insert(playerDto.toModel()).thenApply(Objects::nonNull);
  }

}
