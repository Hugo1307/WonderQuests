package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestRewardModel;
import dev.hugog.minecraft.wonderquests.data.types.RewardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestRewardDto implements Dto<QuestRewardModel> {

  private Integer id;
  private Integer questId;
  private RewardType type;
  private String stringValue;
  private Float numericValue;

  public String obtainRepresentation() {
    if (type == null) {
      return "Unknown";
    }
    return switch (type) {
      case MONEY -> String.format("%f money", numericValue);
      case ITEMS -> String.format("%d %s", numericValue.intValue(), stringValue);
      case EXPERIENCE -> String.format("%f experience", numericValue);
      case COMMAND -> String.format("Command: %s", stringValue);
    };
  }

  @Override
  public QuestRewardModel toModel() {
    return new QuestRewardModel(id, questId, type.name(), stringValue, numericValue);
  }

}
