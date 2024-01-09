package dev.hugog.minecraft.wonderquests.data.models;

import dev.hugog.minecraft.wonderquests.data.dtos.PlayerDto;
import java.util.UUID;

public record PlayerModel(UUID playerId) implements DataModel<PlayerDto> {

    @Override
    public PlayerDto toDto() {
        return new PlayerDto(playerId);
    }

}
