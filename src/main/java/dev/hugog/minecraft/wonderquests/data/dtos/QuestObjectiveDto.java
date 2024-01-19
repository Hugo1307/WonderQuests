package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestObjectiveModel;
import dev.hugog.minecraft.wonderquests.data.types.ObjectiveType;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class QuestObjectiveDto implements Dto<QuestObjectiveModel> {

  private Integer id;
  private Integer questId;
  private ObjectiveType type;
  private String stringValue;
  private Float numericValue;

  public String obtainRepresentation() {
    if (type == null) {
      return "Unknown";
    }
    return switch (type) {
      case KILL_MOBS -> String.format("Kill %d %s", numericValue.intValue(), stringValue);
      case BREAK_BLOCK -> String.format("Break %d %s blocks", numericValue.intValue(), stringValue);
      case PLACE_BLOCK -> String.format("Place %d %s blocks", numericValue.intValue(), stringValue);
      case CRAFT_ITEM -> String.format("Craft %d %s", numericValue.intValue(), stringValue);
      case LOCATION -> String.format("Go to %s", stringValue);
      case KILL_PLAYERS -> String.format("Kill %d players", numericValue.intValue());
    };
  }

  @Override
  public QuestObjectiveModel toModel() {
    return new QuestObjectiveModel(id, questId, type.name(), stringValue, numericValue);
  }

}
