package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.data.types.RequirementType;

public record QuestRequirementModel(
    Integer id,
    Integer questId,
    String type,
    String stringValue,
    Float numericValue
) implements DataModel<QuestRequirementDto> {

  @Override
  public QuestRequirementDto toDto() {
    return new QuestRequirementDto(
        id,
        questId,
        RequirementType.fromString(type),
        stringValue,
        numericValue
    );
  }

}
