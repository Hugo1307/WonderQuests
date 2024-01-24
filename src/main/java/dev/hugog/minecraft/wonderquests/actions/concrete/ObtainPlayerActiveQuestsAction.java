package dev.hugog.minecraft.wonderquests.actions.concrete;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ObtainPlayerActiveQuestsAction extends AbstractAction<CompletableFuture<Set<ActiveQuestDto>>> {

  private final ActiveQuestsService activeQuestsService;

  @Inject
  public ObtainPlayerActiveQuestsAction(@Assisted CommandSender sender, ActiveQuestsService activeQuestsService) {
    super(sender);
    this.activeQuestsService = activeQuestsService;
  }

  @Override
  public CompletableFuture<Set<ActiveQuestDto>> execute() {

    if (!(sender instanceof Player player)) {
      return CompletableFuture.completedFuture(null);
    }

    return activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId());

  }

}
