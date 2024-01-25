package dev.hugog.minecraft.wonderquests.data.dtos.requirements;

import dev.hugog.minecraft.wonderquests.data.dtos.Dto;
import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.types.RequirementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestRequirementDto implements Dto<QuestRequirementModel> {

  private Integer id;
  private Integer questId;
  private RequirementType type;
  private String stringValue;
  private Float numericValue;

  public String obtainRepresentation() {
    if (type == null) {
      return "Unknown";
    }
    return switch (type) {
      case PERMISSION -> "Permission: " + stringValue;
      case ITEM -> "Item: " + stringValue;
      case MONEY -> "Money: " + numericValue;
      case EXPERIENCE -> "Experience: " + numericValue;
    };
  }

  @Override
  public QuestRequirementModel toModel() {
    return new QuestRequirementModel(id, questId, type.name(), stringValue, numericValue);
  }

}
