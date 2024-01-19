package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.requirements.PermissionRequirementDto;
import dev.hugog.minecraft.wonderquests.data.dtos.requirements.QuestRequirementDto;
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

    RequirementType requirementType = RequirementType.fromString(type);

    if (requirementType == RequirementType.PERMISSION) {
      return new PermissionRequirementDto(id, questId, stringValue);
    } else {
      return new QuestRequirementDto(id, questId, RequirementType.fromString(type), stringValue,
          numericValue);
    }

  }

}
