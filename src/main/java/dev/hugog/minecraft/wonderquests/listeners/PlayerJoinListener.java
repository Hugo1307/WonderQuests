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

public class PlayerJoinListener implements Listener {

  private final PlayerService playerService;
  private final ActionsFactory actionsFactory;
  private final SignsMediator signsMediator;
  private final ConcurrencyHandler concurrencyHandler;

  @Inject
  public PlayerJoinListener(PlayerService playerService, ActionsFactory actionsFactory,
      SignsMediator signsMediator, ConcurrencyHandler concurrencyHandler) {

    this.playerService = playerService;
    this.actionsFactory = actionsFactory;
    this.signsMediator = signsMediator;

    this.concurrencyHandler = concurrencyHandler;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {

    Player player = event.getPlayer();
    playerService.checkPlayer(player.getUniqueId());

    actionsFactory.buildShowActiveQuestsAction(player).execute();

    concurrencyHandler.runDelayed(() -> {
      signsMediator.updateQuestsSign(player);
    }, 1, TimeUnit.SECONDS, true);

  }

}
