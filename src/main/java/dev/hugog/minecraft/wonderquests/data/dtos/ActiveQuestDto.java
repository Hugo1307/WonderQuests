package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ActiveQuestDto implements Dto<ActiveQuestModel> {

  private UUID playerId;
  private Integer questId;
  private Integer completedGoals;
  private Float progress;

  @Override
  public ActiveQuestModel toModel() {
    return new ActiveQuestModel(playerId, questId, completedGoals, progress);
  }

}