package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import dev.hugog.minecraft.wonderquests.data.types.RequirementType;
import java.text.DecimalFormat;
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

    DecimalFormat df = new DecimalFormat("#.##");

    return switch (type) {
      case PERMISSION -> "Permission: " + stringValue;
      case ITEM -> "Item: " + stringValue;
      case MONEY -> "Money: " + df.format(numericValue);
      case EXPERIENCE -> "Experience: " + df.format(numericValue);
    };

  }

  @Override
  public QuestRequirementModel toModel() {
    return new QuestRequirementModel(id, questId, type.name(), stringValue, numericValue);
  }

}
