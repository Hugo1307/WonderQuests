package dev.hugog.minecraft.wonderquests.data.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.cache.implementation.ActiveQuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import dev.hugog.minecraft.wonderquests.data.repositories.ActiveQuestRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ActiveQuestsServiceTest {

  @Mock
  private ActiveQuestsCache activeQuestsCache;

  @Mock
  private ActiveQuestRepository activeQuestRepository;

  @Mock
  private QuestDto questDto;

  @InjectMocks
  private ActiveQuestsService activeQuestsService;

  private UUID playerId;
  private Integer questId;
  private Float objectiveTarget;
  private PlayerQuestKey playerQuestKey;
  private ActiveQuestDto activeQuestDto;
  private ActiveQuestDto completedQuestDto;

  @BeforeEach
  public void setUp() {

    playerId = UUID.randomUUID();
    questId = 1;
    objectiveTarget = 10.0f;
    playerQuestKey = new PlayerQuestKey(playerId, questId);
    activeQuestDto = ActiveQuestDto.startQuest(playerId, questId, objectiveTarget);
    completedQuestDto = new ActiveQuestDto(playerId, questId, objectiveTarget, 10.0f,
        System.currentTimeMillis(), questDto);

  }

  @Test
  public void startQuestSuccessfully() {
    when(activeQuestRepository.insert(any(ActiveQuestModel.class)))
        .thenReturn(CompletableFuture.completedFuture(playerQuestKey));

    CompletableFuture<Boolean> result = activeQuestsService.startQuest(playerId, questId,
        objectiveTarget);

    assertTrue(result.join());
  }

  @Test
  public void startQuestFails() {
    when(activeQuestRepository.insert(any(ActiveQuestModel.class)))
        .thenReturn(CompletableFuture.completedFuture(null));

    CompletableFuture<Boolean> result = activeQuestsService.startQuest(playerId, questId,
        objectiveTarget);

    assertFalse(result.join());
  }

  @Test
  public void hasAlreadyStartedQuestReturnsTrue() {
    when(activeQuestRepository.findById(any(PlayerQuestKey.class)))
        .thenReturn(CompletableFuture.completedFuture(Optional.of(activeQuestDto.toModel())));

    CompletableFuture<Boolean> result = activeQuestsService.hasAlreadyStartedQuest(playerId,
        questId);

    assertTrue(result.join());
  }

  @Test
  public void hasAlreadyStartedQuestReturnsFalse() {
    when(activeQuestRepository.findById(any(PlayerQuestKey.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.empty()));

    CompletableFuture<Boolean> result = activeQuestsService.hasAlreadyStartedQuest(playerId,
        questId);

    assertFalse(result.join());
  }

  @Test
  public void getActiveQuestsForPlayerReturnsCachedQuests() {
    Set<ActiveQuestDto> activeQuests = new HashSet<>();
    activeQuests.add(activeQuestDto);
    when(activeQuestsCache.has(any(UUID.class))).thenReturn(true);
    when(activeQuestsCache.get(any(UUID.class))).thenReturn(activeQuests);

    CompletableFuture<Set<ActiveQuestDto>> result = activeQuestsService.getActiveQuestsForPlayer(
        playerId);

    assertEquals(activeQuests, result.join());
  }

  @Test
  public void getActiveQuestsForPlayerReturnsQuestsFromRepository() {
    Set<ActiveQuestDto> activeQuests = new HashSet<>();
    activeQuests.add(activeQuestDto);
    when(activeQuestsCache.has(any(UUID.class))).thenReturn(false);
    when(activeQuestRepository.findAllByPlayerId(any(UUID.class)))
        .thenReturn(CompletableFuture.completedFuture(
            activeQuests.stream().map(ActiveQuestDto::toModel).collect(
                Collectors.toSet())));

    CompletableFuture<Set<ActiveQuestDto>> result = activeQuestsService.getActiveQuestsForPlayer(
        playerId);

    assertEquals(activeQuests, result.join());
  }

  @Test
  public void removeQuestSuccessfully() {
    when(activeQuestRepository.delete(any(PlayerQuestKey.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Void> result = activeQuestsService.removeQuest(playerQuestKey);

    assertNull(result.join());
  }

  @Test
  public void isQuestCompletedReturnsTrue() {
    when(activeQuestsCache.get(any(UUID.class))).thenReturn(Set.of(completedQuestDto));

    boolean result = activeQuestsService.isQuestCompleted(playerId, questId);

    assertTrue(result);
  }

  @Test
  public void isQuestCompletedReturnsFalse() {
    when(activeQuestsCache.get(any(UUID.class))).thenReturn(Set.of(activeQuestDto));

    boolean result = activeQuestsService.isQuestCompleted(playerId, questId);

    assertFalse(result);
  }

  @Test
  public void isQuestExpiredReturnsTrue() {

    when(activeQuestsCache.get(any(UUID.class)))
        .thenReturn(Set.of(completedQuestDto));

    // A low time limit to ensure that the quest is expired
    when(questDto.getTimeLimit()).thenReturn(-1);

    boolean result = activeQuestsService.isQuestExpired(playerId, questId);

    assertTrue(result);

  }

  @Test
  public void isQuestExpiredReturnsFalse() {
    when(activeQuestsCache.get(any(UUID.class))).thenReturn(Set.of(completedQuestDto));

    // A high time limit to ensure that the quest is not expired
    when(questDto.getTimeLimit()).thenReturn(60000);

    boolean result = activeQuestsService.isQuestExpired(playerId, questId);

    assertFalse(result);
  }

  @Test
  public void saveActiveQuestSuccessfully() {
    when(activeQuestRepository.save(any(ActiveQuestModel.class))).thenReturn(
        CompletableFuture.completedFuture(true));

    CompletableFuture<Boolean> result = activeQuestsService.saveActiveQuest(activeQuestDto);

    assertTrue(result.join());
  }

  @Test
  public void saveActiveQuestFails() {
    when(activeQuestRepository.save(any(ActiveQuestModel.class))).thenReturn(
        CompletableFuture.completedFuture(false));

    CompletableFuture<Boolean> result = activeQuestsService.saveActiveQuest(activeQuestDto);

    assertFalse(result.join());
  }
}