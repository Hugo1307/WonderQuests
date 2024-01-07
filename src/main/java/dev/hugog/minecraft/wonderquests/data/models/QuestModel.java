package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;

public record QuestModel(
    Integer id,
    String name,
    String description,
    String openingMsg,
    String closingMsg,
    String item

) implements DataModel<QuestDto> {

  @Override
  public QuestDto toDto() {
    return new QuestDto(id, name, description, openingMsg, closingMsg, item);
  }

}
