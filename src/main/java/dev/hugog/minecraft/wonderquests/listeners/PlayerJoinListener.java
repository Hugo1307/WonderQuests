package dev.hugog.minecraft.wonderquests.listeners;


import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.services.PlayerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private final PlayerService playerService;

  @Inject
  public PlayerJoinListener(PlayerService playerService) {
    this.playerService = playerService;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    playerService.checkPlayer(player.getUniqueId());
  }

}
