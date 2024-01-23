package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;

import java.util.List;

public record QuestModel(
    Integer id,
    String name,
    String description,
    String openingMsg,
    String closingMsg,
    String item,
    Integer timeLimit,
    QuestObjectiveModel objective,
    List<QuestRequirementModel> requirements,
    List<QuestRewardModel> rewards
) implements DataModel<QuestDto> {

  @Override
  public QuestDto toDto() {
    return new QuestDto(id, name, description, openingMsg, closingMsg, item, timeLimit,
        objective != null ? objective.toDto() : null,
        requirements.stream().map(QuestRequirementModel::toDto).toList(),
        rewards.stream().map(QuestRewardModel::toDto).toList()
    );
  }

}
