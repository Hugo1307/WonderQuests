package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.services.PlayerService;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.mediators.SignsMediator;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * This class listens for player join events and handles them accordingly.
 */
public class PlayerJoinListener implements Listener {

  private final PlayerService playerService;
  private final ActionsFactory actionsFactory;
  private final SignsMediator signsMediator;
  private final ConcurrencyHandler concurrencyHandler;

  /**
   * Constructor for the PlayerJoinListener class.
   *
   * @param playerService The service for players.
   * @param actionsFactory The factory for actions.
   * @param signsMediator The mediator for signs.
   * @param concurrencyHandler The handler for concurrency.
   */
  @Inject
  public PlayerJoinListener(PlayerService playerService, ActionsFactory actionsFactory,
      SignsMediator signsMediator, ConcurrencyHandler concurrencyHandler) {

    this.playerService = playerService;
    this.actionsFactory = actionsFactory;
    this.signsMediator = signsMediator;
    this.concurrencyHandler = concurrencyHandler;
  }

  /**
   * This method handles the PlayerJoinEvent.
   *
   * <p>It checks if the player is registered in the database and updates the quests sign for the
   * player.</p>
   *
   * @param event The PlayerJoinEvent to be handled.
   */
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {

    Player player = event.getPlayer();
    playerService.checkPlayer(player.getUniqueId());

    actionsFactory.buildShowActiveQuestsAction(player).execute();

    // Update the quests sign for the player after a delay of 1 second
    concurrencyHandler.runDelayed(() -> {
      signsMediator.updateQuestsSign(player);
    }, 1, TimeUnit.SECONDS, true);

  }

}