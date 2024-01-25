package dev.hugog.minecraft.wonderquests.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@Getter
public class ActiveQuestUpdateEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final QuestUpdateType updateType;

  public ActiveQuestUpdateEvent(Player player, QuestUpdateType updateType) {
    super(true);
    this.player = player;
    this.updateType = updateType;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

}
