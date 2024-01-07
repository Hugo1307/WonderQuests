package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.PlayerDto;
import java.util.UUID;

public class PlayerModel implements DataModel<PlayerDto> {

    private UUID playerId;

    @Override
    public PlayerDto toDto() {
        return null;
    }

}
