package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestObjectiveDto;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;

public record QuestObjectiveModel(
    Integer id,
    Integer questId,
    String type,
    String stringValue,
    Float numericValue
) implements DataModel<QuestObjectiveDto> {

  @Override
  public QuestObjectiveDto toDto() {
    return new QuestObjectiveDto(id, questId, ObjectiveType.fromString(type), stringValue, numericValue);
  }
}
