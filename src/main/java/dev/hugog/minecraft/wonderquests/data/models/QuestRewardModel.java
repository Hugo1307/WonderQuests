package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;

public record QuestRewardModel(
    Integer id,
    Integer questId,
    String type,
    String stringValue,
    Float numericValue
) implements DataModel<QuestRewardDto> {

  @Override
  public QuestRewardDto toDto() {
    return new QuestRewardDto(id, questId, type, stringValue, numericValue);
  }

}
