package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.CompletedQuestModel;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CompletedQuestDto implements Dto<CompletedQuestModel> {

  private UUID playerId;
  private Integer questId;

  @Override
  public CompletedQuestModel toModel() {
    return new CompletedQuestModel(playerId, questId);
  }

}
