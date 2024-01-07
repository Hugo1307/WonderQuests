package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import java.util.UUID;

public class PlayerDto implements Dto<PlayerModel> {

  private UUID playerId;

  @Override
  public PlayerModel toModel() {
    return null;
  }

}
