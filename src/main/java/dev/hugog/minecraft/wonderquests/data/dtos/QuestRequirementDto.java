package dev.hugog.minecraft.wonderquests.data.dtos;

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

  @Override
  public QuestRequirementModel toModel() {
    return new QuestRequirementModel(id, questId, type.toString(), stringValue, numericValue);
  }

}
