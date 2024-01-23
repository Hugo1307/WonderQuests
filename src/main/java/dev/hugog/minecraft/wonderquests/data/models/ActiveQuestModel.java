package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import java.util.UUID;


public record ActiveQuestModel(
    UUID playerId,
    Integer questId,
    Float target,
    Float progress,
    Long startedAt,
    QuestModel questDetails

) implements DataModel<ActiveQuestDto> {

  @Override
  public ActiveQuestDto toDto() {
    return new ActiveQuestDto(playerId, questId, target, progress, startedAt, questDetails.toDto());
  }

}
