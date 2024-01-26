package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestDto implements Dto<QuestModel> {

  private Integer id;

  private String name;

  private String description;

  private String openingMsg;

  private String closingMsg;

  private String item;

  private Integer timeLimit;

  private QuestObjectiveDto objective;

  private List<QuestRequirementDto> requirements;

  private List<QuestRewardDto> rewards;

  @Override
  public QuestModel toModel() {
    return new QuestModel(
        id,
        name,
        description,
        openingMsg,
        closingMsg,
        item,
        timeLimit,
        null,
        null,
        null
    );
  }

}
