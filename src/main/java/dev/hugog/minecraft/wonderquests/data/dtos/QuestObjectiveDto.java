package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class QuestObjectiveDto implements Dto<QuestObjectiveModel> {

  private Integer id;
  private Integer questId;
  private String type;
  private String stringValue;
  private Float numericValue;

  @Override
  public QuestObjectiveModel toModel() {
    return new QuestObjectiveModel(id, questId, type, stringValue, numericValue);
  }

}
