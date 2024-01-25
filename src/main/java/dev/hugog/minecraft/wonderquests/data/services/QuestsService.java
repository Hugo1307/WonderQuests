package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class QuestsService {

  private final QuestsRepository questsRepository;
  private final QuestObjectivesRepository questObjectivesRepository;
  private final QuestRequirementsRepository questRequirementsRepository;
  private final QuestRewardsRepository questRewardsRepository;

  @Inject
  public QuestsService(QuestsRepository questsRepository,
      QuestObjectivesRepository questObjectivesRepository,
      QuestRequirementsRepository questRequirementsRepository,
      QuestRewardsRepository questRewardsRepository) {

    this.questsRepository = questsRepository;
    this.questObjectivesRepository = questObjectivesRepository;
    this.questRequirementsRepository = questRequirementsRepository;
    this.questRewardsRepository = questRewardsRepository;

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

  /**
   * Obtains a quest by its id.
   *
   * @param id The id of the quest to obtain.
   * @return A {@link CompletableFuture} with the {@link QuestDto} if it exists, or an empty
   * {@link Optional} if it doesn't.
   */
  public CompletableFuture<Optional<QuestDto>> getQuestById(Integer id) {

    return questsRepository.findById(id)
        .thenApply(questModelOptional -> questModelOptional.map(QuestModel::toDto));

  }

  public CompletableFuture<Boolean> checkIfQuestExists(Integer id) {
    return questsRepository.findById(id)
        .thenApply(Optional::isPresent);
  }

  public CompletableFuture<Set<QuestDto>> getAvailableQuests(Player player) {
    return questsRepository.findAll().thenApply(questModels -> questModels.stream()
        .map(QuestModel::toDto)
        .filter(quest -> playerHasRequirements(player, quest))
        .collect(Collectors.toSet()));
  }

  public CompletableFuture<List<QuestDto>> getAllQuestsInInterval(Integer start, Integer end) {
    return questsRepository.findAllInInterval(start, end)
        .thenApply(questModels -> questModels.stream()
            .map(QuestModel::toDto)
            .collect(Collectors.toList()));
  }

  public CompletableFuture<Void> deleteQuest(Integer id) {
    return questsRepository.delete(id);
  }

  public boolean playerHasRequirements(Player player, QuestDto quest) {

    boolean hasRequirements = true;

    List<QuestRequirementDto> questRequirements = quest.getRequirements();

    for (QuestRequirementDto requirement : questRequirements) {

      switch (requirement.getType()) {
        case MONEY:
        case ITEM:
        case QUEST_COMPLETED:
        case QUEST_NOT_COMPLETED:
          break;
        case PERMISSION:
          hasRequirements = hasRequirements && player.hasPermission(requirement.getStringValue());
      }

    }

    return hasRequirements;

  }

}
