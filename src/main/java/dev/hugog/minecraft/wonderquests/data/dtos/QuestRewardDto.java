package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class QuestRewardDto implements Dto<QuestRewardModel> {

  private Integer id;
  private Integer questId;
  private String type;
  private String stringValue;
  private Float numericValue;

  @Override
  public QuestRewardModel toModel() {
    return new QuestRewardModel(id, questId, type, stringValue, numericValue);
  }

}
