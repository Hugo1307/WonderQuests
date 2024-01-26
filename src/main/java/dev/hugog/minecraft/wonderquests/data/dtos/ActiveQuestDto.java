package dev.hugog.minecraft.wonderquests.data.dtos;

import dev.hugog.minecraft.wonderquests.data.models.ActiveQuestModel;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ActiveQuestDto implements Dto<ActiveQuestModel> {

  private UUID playerId;
  private Integer questId;
  private Float target;
  private Float progress;
  private Long startedAt;
  private QuestDto questDetails;

  public static ActiveQuestDto startQuest(UUID playerId, Integer questId, Float objectiveTarget) {
    return new ActiveQuestDto(playerId, questId, objectiveTarget, 0f, System.currentTimeMillis(),
        null);
  }

  public int getProgressPercentage() {
    return (int) ((progress / target) * 100);
  }

  public int getSecondsLeft() {
    return (int) (questDetails.getTimeLimit() * 1000 - (System.currentTimeMillis() - startedAt))
        / 1000;
  }

  public boolean isExpired() {
    return getQuestDetails().getTimeLimit() * 1000 - (System.currentTimeMillis() - getStartedAt()) < 0;
  }

  @Override
  public ActiveQuestModel toModel() {
    return new ActiveQuestModel(playerId, questId, target, progress, startedAt,
        questDetails != null ? questDetails.toModel() : null);
  }

}
