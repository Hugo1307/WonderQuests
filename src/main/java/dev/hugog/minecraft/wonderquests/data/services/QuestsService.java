package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.cache.implementation.QuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRequirementDto;
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

/**
 * This class provides services for managing quests in the game.
 */
public class QuestsService {

  private final QuestsRepository questsRepository;
  private final QuestObjectivesRepository questObjectivesRepository;
  private final QuestRequirementsRepository questRequirementsRepository;
  private final QuestRewardsRepository questRewardsRepository;
  private final QuestsCache questsCache;

  /**
   * Constructor for the QuestsService class.
   *
   * @param questsRepository The repository instance used for database operations related to quests.
   * @param questObjectivesRepository The repository instance used for database operations related to quest objectives.
   * @param questRequirementsRepository The repository instance used for database operations related to quest requirements.
   * @param questRewardsRepository The repository instance used for database operations related to quest rewards.
   * @param questsCache The cache instance used for caching quests.
   */
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

  /**
   * This method creates a new quest.
   *
   * @param questDto The DTO of the quest.
   * @return a CompletableFuture that will be completed with the id of the created quest.
   */
  public CompletableFuture<Integer> createNewQuest(QuestDto questDto) {
    QuestModel questModel = questDto.toModel();
    return questsRepository.insert(questModel);
  }

  /**
   * This method adds a quest objective.
   *
   * @param questObjectiveDto The DTO of the quest objective.
   * @return a CompletableFuture that will be completed with the id of the added quest objective.
   */
  public CompletableFuture<Integer> addQuestObjective(QuestObjectiveDto questObjectiveDto) {
    QuestObjectiveModel questObjectiveModel = questObjectiveDto.toModel();
    return questObjectivesRepository.insert(questObjectiveModel);
  }

  /**
   * This method adds a quest requirement.
   *
   * @param questRequirementDto The DTO of the quest requirement.
   * @return a CompletableFuture that will be completed with the id of the added quest requirement.
   */
  public CompletableFuture<Integer> addQuestRequirement(QuestRequirementDto questRequirementDto) {
    QuestRequirementModel questRequirementModel = questRequirementDto.toModel();
    return questRequirementsRepository.insert(questRequirementModel);
  }

  /**
   * This method adds a quest reward.
   *
   * @param questRewardDto The DTO of the quest reward.
   * @return a CompletableFuture that will be completed with the id of the added quest reward.
   */
  public CompletableFuture<Integer> addQuestReward(QuestRewardDto questRewardDto) {
    QuestRewardModel questRewardModel = questRewardDto.toModel();
    return questRewardsRepository.insert(questRewardModel);
  }

  /**
   * This method retrieves a quest by its id.
   *
   * @param id The id of the quest.
   * @return a CompletableFuture that will be completed with an Optional containing the quest if it exists, or empty if it does not.
   */
  public CompletableFuture<Optional<QuestDto>> getQuestById(Integer id) {
    return questsRepository.findById(id)
        .thenApply(questModel -> questModel.map(QuestModel::toDto));
  }

  /**
   * This method retrieves a quest by its id, with an option to use cache.
   *
   * @param id The id of the quest.
   * @param useCache A boolean indicating whether to use cache for retrieving the quest.
   * @return a CompletableFuture that will be completed with an Optional containing the quest if it exists, or empty if it does not.
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

  /**
   * This method checks if a quest exists.
   *
   * @param id The id of the quest.
   * @return a CompletableFuture that will be completed with a boolean indicating if the quest exists.
   */
  public CompletableFuture<Boolean> checkIfQuestExists(Integer id) {
    return questsRepository.findById(id)
        .thenApply(Optional::isPresent);
  }

  /**
   * This method retrieves all quests in a given interval.
   *
   * @param start The start of the interval.
   * @param end The end of the interval.
   * @return a CompletableFuture that will be completed with a list of quests in the given interval.
   */
  public CompletableFuture<List<QuestDto>> getAllQuestsInInterval(Integer start, Integer end) {
    return questsRepository.findAllInInterval(start, end)
        .thenApply(questModels -> questModels.stream()
            .map(QuestModel::toDto)
            .collect(Collectors.toList()));
  }

  /**
   * This method retrieves all quests.
   *
   * @return a CompletableFuture that will be completed with a list of all quests.
   */
  public CompletableFuture<List<QuestDto>> getAllQuests() {
    return questsRepository.findAll()
        .thenApply(questModels -> questModels.stream()
            .map(QuestModel::toDto)
            .collect(Collectors.toList()));
  }

  /**
   * This method deletes a quest.
   *
   * @param id The id of the quest.
   * @return a CompletableFuture that will be completed when the quest is deleted.
   */
  public CompletableFuture<Void> deleteQuest(Integer id) {
    return questsRepository.delete(id);
  }

}