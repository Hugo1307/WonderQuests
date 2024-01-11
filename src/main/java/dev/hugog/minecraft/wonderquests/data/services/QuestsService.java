package dev.hugog.minecraft.wonderquests.data.services;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
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

  public CompletableFuture<Optional<QuestDto>> getQuestById(Integer id) {

    return questsRepository.findById(id)
        .thenApplyAsync(questModel -> {

          Optional<QuestDto> questDto = questModel.map(QuestModel::toDto);

          CompletableFuture<?> completableFuture1 = questObjectivesRepository.findAllByQuestId(id).thenAccept(
              questObjectiveModels -> {
                List<QuestObjectiveDto> questObjectiveDtos = questObjectiveModels.stream()
                    .map(QuestObjectiveModel::toDto)
                    .toList();

                questDto.ifPresent(dto -> dto.setObjectives(questObjectiveDtos));

              });

          CompletableFuture<?> completableFuture2 = questRequirementsRepository.findAllByQuestId(id).thenAccept(
              questRequirementModels -> {
                List<QuestRequirementDto> questRequirementDtos = questRequirementModels.stream()
                    .map(QuestRequirementModel::toDto)
                    .toList();

                questDto.ifPresent(dto -> dto.setRequirements(questRequirementDtos));

              });

          CompletableFuture<?> completableFuture3 = questRewardsRepository.findAllByQuestId(id).thenAccept(
              questRewardModels -> {
                List<QuestRewardDto> questRewardDtos = questRewardModels.stream()
                    .map(QuestRewardModel::toDto)
                    .toList();

                questDto.ifPresent(dto -> dto.setRewards(questRewardDtos));

              });

          return CompletableFuture.allOf(completableFuture1, completableFuture2, completableFuture3)
              .thenApply(aVoid -> questDto)
              .join();

        });
  }

  public CompletableFuture<Boolean> checkIfQuestExists(Integer id) {
    return questsRepository.findById(id)
        .thenApply(Optional::isPresent);
  }

}
