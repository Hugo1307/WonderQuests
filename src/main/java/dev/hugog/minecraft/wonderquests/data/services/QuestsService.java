package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QuestsService {

  private final QuestsRepository questsRepository;
  private final QuestObjectivesRepository questObjectivesRepository;
  private final QuestRequirementsRepository questRequirementsRepository;
  private final QuestRewardsRepository questRewardsRepository;
  private final QuestsCache questsCache;

  @Inject
  public QuestsService(QuestsRepository questsRepository,
      QuestObjectivesRepository questObjectivesRepository,
      QuestRequirementsRepository questRequirementsRepository,
      QuestRewardsRepository questRewardsRepository, QuestsCache questsCache) {

    this.questsRepository = questsRepository;
    this.questObjectivesRepository = questObjectivesRepository;
    this.questRequirementsRepository = questRequirementsRepository;
    this.questRewardsRepository = questRewardsRepository;
    this.questsCache = questsCache;

  }

  public CompletableFuture<Integer> createNewQuest(QuestDto questDto) {
    QuestModel questModel = questDto.toModel();
    return questsRepository.insert(questModel);
  }

  public CompletableFuture<Integer> addQuestObjective(QuestObjectiveDto questObjectiveDto) {
    QuestObjectiveModel questObjectiveModel = questObjectiveDto.toModel();
    return questObjectivesRepository.insert(questObjectiveModel);
  }

  public CompletableFuture<Integer> addQuestRequirement(QuestRequirementDto questRequirementDto) {
    QuestRequirementModel questRequirementModel = questRequirementDto.toModel();
    return questRequirementsRepository.insert(questRequirementModel);
  }

  public CompletableFuture<Integer> addQuestReward(QuestRewardDto questRewardDto) {
    QuestRewardModel questRewardModel = questRewardDto.toModel();
    return questRewardsRepository.insert(questRewardModel);
  }

  public CompletableFuture<Optional<QuestDto>> getQuestById(Integer id) {
    return questsRepository.findById(id)
        .thenApply(questModel -> questModel.map(QuestModel::toDto));
  }

  /**
   * <h4>Retrieves a quest by its ID, with an option to use cache.</h4>
   * <br>
   * <p>If the quest is found, the Optional will contain the QuestDto.</p>
   * <p>If the quest is not found, the Optional will be empty.</p>
   * <p>If useCache is true and the quest is in the cache, the quest will be retrieved the cache.</p>
   * <p>If useCache is true and the quest is not in the cache, the quest will be retrieved from the repository and then stored in the cache for future requests.</p>
   * <p>If useCache is false, the quest will be retrieved directly from the repository.</p>
   *
   * @param id       The ID of the quest to retrieve.
   * @param useCache A boolean indicating whether to use cache for retrieving the quest.
   * @return A CompletableFuture that, when completed, will contain an Optional<QuestDto>.
   */
  public CompletableFuture<Optional<QuestDto>> getQuestById(Integer id, boolean useCache) {

    // If cache is not to be used, retrieve the quest directly from the repository
    if (!useCache) {
      return getQuestById(id);
    }

    // If the quest is in the cache, retrieve it from there
    if (questsCache.has(id)) {
      return CompletableFuture.completedFuture(Optional.of(questsCache.get(id)));
    }

    // If the quest is not in the cache, retrieve it from the repository and store it in the cache for future requests
    return questsRepository.findById(id).thenApply(questModel -> {
      Optional<QuestDto> questDto = questModel.map(QuestModel::toDto);
      questDto.ifPresent(dto -> questsCache.put(id, dto));
      return questDto;
    });

  }

  public CompletableFuture<Boolean> checkIfQuestExists(Integer id) {
    return questsRepository.findById(id)
        .thenApply(Optional::isPresent);
  }

  public CompletableFuture<List<QuestDto>> getAllQuestsInInterval(Integer start, Integer end) {
    return questsRepository.findAllInInterval(start, end)
        .thenApply(questModels -> questModels.stream()
            .map(QuestModel::toDto)
            .collect(Collectors.toList()));
  }

  public CompletableFuture<List<QuestDto>> getAllQuests() {
    return questsRepository.findAll()
        .thenApply(questModels -> questModels.stream()
            .map(QuestModel::toDto)
            .collect(Collectors.toList()));
  }

  public CompletableFuture<Void> deleteQuest(Integer id) {
    return questsRepository.delete(id);
  }

}
