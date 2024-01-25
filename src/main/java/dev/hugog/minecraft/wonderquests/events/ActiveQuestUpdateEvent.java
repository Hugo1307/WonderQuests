package dev.hugog.minecraft.wonderquests.events;

import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@Getter
public class ActiveQuestUpdateEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final QuestUpdateType updateType;
  private final ActiveQuestDto activeQuest;

  public ActiveQuestUpdateEvent(Player player, QuestUpdateType updateType,
      ActiveQuestDto activeQuest) {

    super(true);
    this.player = player;
    this.updateType = updateType;
    this.activeQuest = activeQuest;

  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

}
