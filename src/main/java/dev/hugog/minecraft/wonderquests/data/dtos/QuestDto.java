package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.QuestModel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class QuestDto implements Dto<QuestModel> {

  private Integer id;

  private String name;

  private String description;

  private String openingMsg;

  private String closingMsg;

  private String item;

  private Integer timeLimit;

  private List<QuestObjectiveDto> objectives;

  private List<QuestRequirementDto> requirements;

  private List<QuestRewardDto> rewards;

  public QuestDto(Integer id, String name, String description, String openingMsg, String closingMsg,
      String item, Integer timeLimit) {

    this.id = id;
    this.name = name;
    this.description = description;
    this.openingMsg = openingMsg;
    this.closingMsg = closingMsg;
    this.item = item;
    this.timeLimit = timeLimit;

  }

  @Override
  public QuestModel toModel() {
    return new QuestModel(
        id,
        name,
        description,
        openingMsg,
        closingMsg,
        item,
        timeLimit
    );
  }

}
