package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestRequirementModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class QuestRequirementDto implements Dto<QuestRequirementModel> {

  private Integer id;
  private Integer questId;
  private String type;
  private String stringValue;
  private Float numericValue;

  @Override
  public QuestRequirementModel toModel() {
    return new QuestRequirementModel(id, questId, type, stringValue, numericValue);
  }

}
