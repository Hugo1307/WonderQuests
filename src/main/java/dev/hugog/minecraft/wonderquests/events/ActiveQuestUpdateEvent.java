package dev.hugog.minecraft.wonderquests.events;

import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This class represents an event that is triggered when an active quest is updated.
 */
@Getter
public class ActiveQuestUpdateEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final Player player;
  private final QuestUpdateType updateType;
  private final ActiveQuestDto activeQuest;

  /**
   * Constructor for the ActiveQuestUpdateEvent class.
   *
   * @param player The player who has an active quest that is updated.
   * @param updateType The type of update that occurred on the active quest.
   * @param activeQuest The active quest that was updated.
   */
  public ActiveQuestUpdateEvent(Player player, QuestUpdateType updateType,
      ActiveQuestDto activeQuest) {

    super(true);
    this.player = player;
    this.updateType = updateType;
    this.activeQuest = activeQuest;

  }

  /**
   * This method gets the list of event handlers for this event.
   *
   * @return a HandlerList containing all the event handlers for this event.
   */
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  /**
   * This method gets the list of event handlers for this event.
   *
   * @return a HandlerList containing all the event handlers for this event.
   */
  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

}