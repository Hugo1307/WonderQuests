package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import java.util.UUID;


public record ActiveQuestModel(
    UUID playerId,
    Integer questId,
    Integer completedGoals,
    Float progress,
    Long startedAt,
    QuestModel questDetails

) implements DataModel<ActiveQuestDto> {

  @Override
  public ActiveQuestDto toDto() {
    return new ActiveQuestDto(playerId, questId, completedGoals, progress, startedAt, questDetails.toDto());
  }

}
