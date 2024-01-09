package dev.hugog.minecraft.wonderquests.data.keys;


import java.util.UUID;

public record PlayerQuestKey(
    UUID playerId,
    Integer questId
) {

}
