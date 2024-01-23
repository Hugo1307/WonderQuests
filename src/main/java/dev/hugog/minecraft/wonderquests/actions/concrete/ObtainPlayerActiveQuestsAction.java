package dev.hugog.minecraft.wonderquests.actions.concrete;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.cache.ActiveQuestsCache;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.services.PlayerService;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ObtainPlayerActiveQuestsAction extends AbstractAction<CompletableFuture<Set<ActiveQuestDto>>> {

  private final ActiveQuestsCache activeQuestsCache;
  private final PlayerService playerService;

  @Inject
  public ObtainPlayerActiveQuestsAction(@Assisted CommandSender sender, ActiveQuestsCache activeQuestsCache, PlayerService playerService) {
    super(sender);
    this.activeQuestsCache = activeQuestsCache;
    this.playerService = playerService;
  }

  @Override
  public CompletableFuture<Set<ActiveQuestDto>> execute() {

    if (!(sender instanceof Player player)) {
      return CompletableFuture.completedFuture(null);
    }

    boolean areActiveQuestsCached = activeQuestsCache.has(player.getUniqueId());

    if (areActiveQuestsCached) {
      return CompletableFuture.completedFuture(activeQuestsCache.get(player.getUniqueId()));
    } else {
      return playerService.getCurrentActiveQuests(player.getUniqueId())
          .thenApply(activeQuests -> {
            activeQuestsCache.put(player.getUniqueId(), activeQuests);
            return activeQuests;
          });
    }

  }

}
