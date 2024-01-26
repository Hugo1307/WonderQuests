package dev.hugog.minecraft.wonderquests.data.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.hugog.minecraft.wonderquests.cache.implementation.QuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
import dev.hugog.minecraft.wonderquests.data.dtos.requirements.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestObjectivesRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestsRepository;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import dev.hugog.minecraft.wonderquests.data.types.RequirementType;
import dev.hugog.minecraft.wonderquests.data.types.RewardType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QuestsServiceTest {

  @Mock
  private QuestsRepository questsRepository;

  @Mock
  private QuestRequirementsRepository questRequirementsRepository;

  @Mock
  private QuestRewardsRepository questRewardsRepository;

  @Mock
  private QuestObjectivesRepository questObjectivesRepository;

  @Mock
  private QuestsCache questsCache;

  @InjectMocks
  private QuestsService questsService;

  private Integer questId;
  private QuestDto questDto;

  @BeforeEach
  public void setUp() {
    questId = 1;
    questDto = new QuestDto(
        questId,
        "Test Quest",
        "Test Description",
        "1",
        "1",
        "1",
        1,
        null,
        null,
        null
    );
  }

  @Test
  public void createNewQuestSuccessfully() {

    when(questsRepository.insert(any(QuestModel.class))).thenReturn(
        CompletableFuture.completedFuture(questId));

    CompletableFuture<Integer> result = questsService.createNewQuest(questDto);

    assertEquals(questId, result.join());

  }

  @Test
  public void createNewQuestFails() {

    when(questsRepository.insert(any(QuestModel.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Integer> result = questsService.createNewQuest(questDto);

    assertNull(result.join());

  }

  @Test
  public void getQuestByIdReturnsQuest() {

    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.of(questDto.toModel())));

    CompletableFuture<Optional<QuestDto>> result = questsService.getQuestById(questId);

    assertTrue(result.join().isPresent());
    assertEquals(questDto, result.join().get());

  }

  @Test
  public void getQuestByIdReturnsEmpty() {

    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.empty()));

    CompletableFuture<Optional<QuestDto>> result = questsService.getQuestById(questId);

    assertFalse(result.join().isPresent());

  }

  @Test
  public void checkIfQuestExistsReturnsTrue() {

    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.of(questDto.toModel())));

    CompletableFuture<Boolean> result = questsService.checkIfQuestExists(questId);

    assertTrue(result.join());

  }

  @Test
  public void checkIfQuestExistsReturnsFalse() {

    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.empty()));

    CompletableFuture<Boolean> result = questsService.checkIfQuestExists(questId);

    assertFalse(result.join());

  }

  @Test
  public void deleteQuestSuccessfully() {

    when(questsRepository.delete(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Void> result = questsService.deleteQuest(questId);

    assertNull(result.join());

  }

  @Test
  public void getQuestByIdWithCacheDisabledReturnsQuest() {

    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.of(questDto.toModel())));

    CompletableFuture<Optional<QuestDto>> result = questsService.getQuestById(questId, false);

    assertTrue(result.join().isPresent());
    assertEquals(questDto, result.join().get());

  }

  @Test
  public void getQuestByIdWithCacheEnabledAndQuestInCacheReturnsQuest() {

    when(questsCache.has(any(Integer.class))).thenReturn(true);
    when(questsCache.get(any(Integer.class))).thenReturn(questDto);

    CompletableFuture<Optional<QuestDto>> result = questsService.getQuestById(questId, true);

    assertTrue(result.join().isPresent());
    assertEquals(questDto, result.join().get());

  }

  @Test
  public void getQuestByIdWithCacheEnabledAndQuestNotInCacheReturnsQuest() {

    when(questsCache.has(any(Integer.class))).thenReturn(false);
    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.of(questDto.toModel())));

    CompletableFuture<Optional<QuestDto>> result = questsService.getQuestById(questId, true);

    assertTrue(result.join().isPresent());
    assertEquals(questDto, result.join().get());

  }

  @Test
  public void getQuestByIdWithCacheEnabledAndQuestNotInCacheStoresQuestInCache() {

    when(questsCache.has(any(Integer.class))).thenReturn(false);
    when(questsRepository.findById(any(Integer.class))).thenReturn(
        CompletableFuture.completedFuture(Optional.of(questDto.toModel())));

    questsService.getQuestById(questId, true);

    verify(questsCache).put(eq(questId), any(QuestDto.class));

  }

  @Test
  public void addQuestObjectiveSuccessfully() {

    int questObjectiveId = 1;

    QuestObjectiveDto questObjectiveDto = new QuestObjectiveDto(questObjectiveId, 1,
        ObjectiveType.BREAK_BLOCK, "SAND", 1F);

    when(questObjectivesRepository.insert(any(QuestObjectiveModel.class))).thenReturn(
        CompletableFuture.completedFuture(questObjectiveId));

    CompletableFuture<Integer> result = questsService.addQuestObjective(questObjectiveDto);

    assertEquals(1, result.join());

  }

  @Test
  public void addQuestObjectiveFails() {

    QuestObjectiveDto questObjectiveDto = new QuestObjectiveDto(1, 1,
        ObjectiveType.BREAK_BLOCK, "SAND", 1F);

    when(questObjectivesRepository.insert(any(QuestObjectiveModel.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Integer> result = questsService.addQuestObjective(questObjectiveDto);

    assertNull(result.join());

  }

  @Test
  public void addQuestRequirementSuccessfully() {

    int questRequirementId = 1;
    QuestRequirementDto questRequirementDto = new QuestRequirementDto(
        questRequirementId, 1, RequirementType.MONEY, null, 1F);

    when(questRequirementsRepository.insert(any(QuestRequirementModel.class))).thenReturn(
        CompletableFuture.completedFuture(questRequirementId));

    CompletableFuture<Integer> result = questsService.addQuestRequirement(questRequirementDto);

    assertEquals(1, result.join());

  }

  @Test
  public void addQuestRequirementFails() {

    QuestRequirementDto questRequirementDto = new QuestRequirementDto(
        1, 1, RequirementType.MONEY, null, 1F);

    when(questRequirementsRepository.insert(any(QuestRequirementModel.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Integer> result = questsService.addQuestRequirement(questRequirementDto);

    assertNull(result.join());

  }

  @Test
  public void addQuestRewardSuccessfully() {

    int questRewardId = 1;
    QuestRewardDto questRewardDto = new QuestRewardDto(
        questRewardId, 1, RewardType.MONEY, "", 1F);

    when(questRewardsRepository.insert(any(QuestRewardModel.class))).thenReturn(
        CompletableFuture.completedFuture(questRewardId));

    CompletableFuture<Integer> result = questsService.addQuestReward(questRewardDto);

    assertEquals(1, result.join());

  }

  @Test
  public void addQuestRewardFails() {

    QuestRewardDto questRewardDto = new QuestRewardDto(
        1, 1, RewardType.MONEY, "", 1F);

    when(questRewardsRepository.insert(any(QuestRewardModel.class))).thenReturn(
        CompletableFuture.completedFuture(null));

    CompletableFuture<Integer> result = questsService.addQuestReward(questRewardDto);

    assertNull(result.join());

  }

  @Test
  public void getAllQuestsInIntervalReturnsQuests() {

    List<QuestModel> questModels = List.of(questDto.toModel(), questDto.toModel());

    when(questsRepository.findAllInInterval(any(Integer.class), any(Integer.class)))
        .thenReturn(CompletableFuture.completedFuture(questModels));

    CompletableFuture<List<QuestDto>> result = questsService.getAllQuestsInInterval(1, 2);

    assertFalse(result.join().isEmpty());
    assertEquals(2, result.join().size());

  }

  @Test
  public void getAllQuestsInIntervalReturnsEmpty() {

    when(questsRepository.findAllInInterval(any(Integer.class), any(Integer.class)))
        .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

    CompletableFuture<List<QuestDto>> result = questsService.getAllQuestsInInterval(1, 2);

    assertTrue(result.join().isEmpty());

  }

  @Test
  public void getAllQuestsReturnsQuests() {

    Set<QuestModel> questModels = Set.of(questDto.toModel());
    when(questsRepository.findAll()).thenReturn(CompletableFuture.completedFuture(questModels));

    CompletableFuture<List<QuestDto>> result = questsService.getAllQuests();

    assertFalse(result.join().isEmpty());
    assertEquals(1, result.join().size());

  }

  @Test
  public void getAllQuestsReturnsEmpty() {

    when(questsRepository.findAll())
        .thenReturn(CompletableFuture.completedFuture(new HashSet<>()));

    CompletableFuture<List<QuestDto>> result = questsService.getAllQuests();

    assertTrue(result.join().isEmpty());

  }

}