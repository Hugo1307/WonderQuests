package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
import dev.hugog.minecraft.wonderquests.data.dtos.requirements.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import dev.hugog.minecraft.wonderquests.data.repositories.ActiveQuestRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestObjectivesRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestsRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class QuestsService {

  private final QuestsRepository questsRepository;
  private final QuestObjectivesRepository questObjectivesRepository;
  private final QuestRequirementsRepository questRequirementsRepository;
  private final QuestRewardsRepository questRewardsRepository;
  private final ActiveQuestRepository activeQuestRepository;

  @Inject
  public QuestsService(QuestsRepository questsRepository,
      QuestObjectivesRepository questObjectivesRepository,
      QuestRequirementsRepository questRequirementsRepository,
      QuestRewardsRepository questRewardsRepository,
      ActiveQuestRepository activeQuestRepository) {

    this.questsRepository = questsRepository;
    this.questObjectivesRepository = questObjectivesRepository;
    this.questRequirementsRepository = questRequirementsRepository;
    this.questRewardsRepository = questRewardsRepository;
    this.activeQuestRepository = activeQuestRepository;

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
    return questsRepository.findById(id).thenApply(questModel -> questModel.map(QuestModel::toDto));
  }

  public CompletableFuture<Boolean> checkIfQuestExists(Integer id) {
    return questsRepository.findById(id)
        .thenApply(Optional::isPresent);
  }

}
