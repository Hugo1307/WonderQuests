package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.SignDto;
import dev.hugog.minecraft.wonderquests.data.types.SignType;

public record SignModel(
    Integer id,
    String type,
    String worldName,
    Integer x,
    Integer y,
    Integer z
) implements DataModel<SignDto> {

  @Override
  public SignDto toDto() {
    return new SignDto(id, SignType.fromString(type), worldName, x, y, z);
  }

}
