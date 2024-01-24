package dev.hugog.minecraft.wonderquests.listeners;


import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.services.PlayerService;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private final PlayerService playerService;
  private final ActionsFactory actionsFactory;

  @Inject
  public PlayerJoinListener(PlayerService playerService, ActionsFactory actionsFactory) {
    this.playerService = playerService;
    this.actionsFactory = actionsFactory;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {

    Player player = event.getPlayer();
    playerService.checkPlayer(player.getUniqueId());

    actionsFactory.buildShowActiveQuestsAction(player).execute();

  }

}
