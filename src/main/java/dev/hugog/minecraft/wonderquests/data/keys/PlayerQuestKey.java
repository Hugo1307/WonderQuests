package dev.hugog.minecraft.wonderquests.data.keys;


import java.util.Objects;
import java.util.UUID;

public record PlayerQuestKey(
    UUID playerId,
    Integer questId
) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlayerQuestKey that = (PlayerQuestKey) o;
    return Objects.equals(playerId, that.playerId) && Objects.equals(questId,
        that.questId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playerId, questId);
  }

}
