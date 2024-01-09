package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.PlayerModel;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PlayerDto implements Dto<PlayerModel> {

  private UUID playerId;

  @Override
  public PlayerModel toModel() {
    return new PlayerModel(playerId);
  }

}
