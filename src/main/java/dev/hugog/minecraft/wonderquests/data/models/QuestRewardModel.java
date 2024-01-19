package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestRewardDto;
import dev.hugog.minecraft.wonderquests.data.types.RewardType;

public record QuestRewardModel(
    Integer id,
    Integer questId,
    String type,
    String stringValue,
    Float numericValue
) implements DataModel<QuestRewardDto> {

  @Override
  public QuestRewardDto toDto() {
    return new QuestRewardDto(id, questId, RewardType.fromString(type), stringValue, numericValue);
  }

}
