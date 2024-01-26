package dev.hugog.minecraft.wonderquests.data.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.data.dtos.CompletedQuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.CompletedQuestModel;
import dev.hugog.minecraft.wonderquests.data.repositories.CompletedQuestRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CompletedQuestsServiceTest {

  @Mock
  private CompletedQuestRepository completedQuestRepository;

  @InjectMocks
  private CompletedQuestsService completedQuestsService;

  private UUID playerId;
  private Integer questId;
  private CompletedQuestDto completedQuestDto;
  private PlayerQuestKey playerQuestKey;

  @BeforeEach
  public void setUp() {

    playerId = UUID.randomUUID();
    questId = 1;

    playerQuestKey = new PlayerQuestKey(playerId, questId);
    completedQuestDto = new CompletedQuestDto(playerId, questId);

  }

  @Test
  @DisplayName("Add completed quest is successful on database layer")
  public void addCompletedQuestSuccessfully() {

    when(completedQuestRepository.insert(any(CompletedQuestModel.class)))
        .thenReturn(CompletableFuture.completedFuture(playerQuestKey));

    CompletableFuture<Boolean> result = completedQuestsService.addCompletedQuest(completedQuestDto);

    assertTrue(result.join());

  }

  @Test
  @DisplayName("Add completed quest fails on database layer")
  public void addCompletedQuestFails() {

    when(completedQuestRepository.insert(any(CompletedQuestModel.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Boolean> result = completedQuestsService.addCompletedQuest(completedQuestDto);

    assertFalse(result.join());

  }

  @Test
  @DisplayName("Get completed quest by player returns quests")
  public void getCompletedQuestByPlayerReturnsQuests() {

    Set<CompletedQuestModel> completedQuests = new HashSet<>();

    completedQuests.add(completedQuestDto.toModel());
    when(completedQuestRepository.findAllByPlayer(any(UUID.class))).thenReturn(
        CompletableFuture.completedFuture(completedQuests));

    CompletableFuture<Set<CompletedQuestDto>> result = completedQuestsService
        .getCompletedQuestByPlayer(playerId);

    assertEquals(
        completedQuests.stream().map(CompletedQuestModel::toDto).collect(Collectors.toSet()),
        result.join());

  }

  @Test
  @DisplayName("Get completed quest by player returns empty set")
  public void getCompletedQuestByPlayerReturnsEmptySet() {

    when(completedQuestRepository.findAllByPlayer(any(UUID.class))).thenReturn(
        CompletableFuture.completedFuture(new HashSet<>()));

    CompletableFuture<Set<CompletedQuestDto>> result = completedQuestsService.getCompletedQuestByPlayer(
        playerId);

    assertTrue(result.join().isEmpty());

  }

  @Test
  @DisplayName("Has completed quest returns true")
  public void hasCompletedQuestReturnsTrue() {

    when(completedQuestRepository.findAllByPlayer(any(UUID.class))).thenReturn(
        CompletableFuture.completedFuture(Set.of(completedQuestDto.toModel())));

    CompletableFuture<Boolean> result = completedQuestsService.hasCompletedQuest(playerId, questId);

    assertTrue(result.join());

  }

  @Test
  @DisplayName("Has completed quest returns false")
  public void hasCompletedQuestReturnsFalse() {

    when(completedQuestRepository.findAllByPlayer(any(UUID.class))).thenReturn(
        CompletableFuture.completedFuture(new HashSet<>()));

    CompletableFuture<Boolean> result = completedQuestsService.hasCompletedQuest(playerId, questId);

    assertFalse(result.join());

  }
}