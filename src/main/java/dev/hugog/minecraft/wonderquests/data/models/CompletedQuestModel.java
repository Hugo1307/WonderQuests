package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.CompletedQuestDto;
import java.util.UUID;

public record CompletedQuestModel(
    UUID playerId,
    Integer questId
) implements DataModel<CompletedQuestDto> {

  @Override
  public CompletedQuestDto toDto() {
    return new CompletedQuestDto(playerId, questId);
  }

}
