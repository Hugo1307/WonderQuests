package dev.hugog.minecraft.wonderquests.data.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

  @Mock
  private PlayersRepository playersRepository;

  @InjectMocks
  private PlayerService playerService;

  private UUID playerId;

  @BeforeEach
  public void setUp() {
    playerId = UUID.randomUUID();
  }

  @Test
  @DisplayName("Check player returns true when the player exists in the database")
  public void checkPlayerReturnsTrueWhenPlayerExists() {

    when(playersRepository.findById(any(UUID.class)))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(new PlayerModel(playerId))));

    CompletableFuture<Boolean> result = playerService.checkPlayer(playerId);

    assertTrue(result.join());

  }

  @Test
  @DisplayName("Check player returns true when the player does not exist in the database")
  public void checkPlayerReturnsTrueWhenPlayerDoesNotExist() {

    when(playersRepository.findById(any(UUID.class)))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    when(playersRepository.insert(any(PlayerModel.class)))
        .thenReturn(CompletableFuture.completedFuture(playerId));

    CompletableFuture<Boolean> result = playerService.checkPlayer(playerId);

    assertTrue(result.join());
  }

  @Test
  @DisplayName("Create player returns true when player is successfully created")
  public void createPlayerReturnsTrueWhenPlayerIsCreated() {

    when(playersRepository.insert(any(PlayerModel.class)))
        .thenReturn(CompletableFuture.completedFuture(playerId));

    CompletableFuture<Boolean> result = playerService.createPlayer(playerId);

    assertTrue(result.join());

  }

  @Test
  @DisplayName("Create player returns false when player creation fails")
  public void createPlayerReturnsFalseWhenPlayerCreationFails() {

    when(playersRepository.insert(any(PlayerModel.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<Boolean> result = playerService.createPlayer(playerId);

    assertFalse(result.join());

  }

}