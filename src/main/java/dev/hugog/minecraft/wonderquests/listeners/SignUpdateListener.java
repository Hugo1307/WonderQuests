package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignUpdateListener implements Listener {

  private final ActiveQuestsService activeQuestsService;

  @Inject
  public SignUpdateListener(ActiveQuestsService activeQuestsService) {
    this.activeQuestsService = activeQuestsService;
  }

  @EventHandler
  public void onSignChanged(SignChangeEvent event) {

    List<Component> lines = event.lines();

    if (lines.get(0) == null) {
      return;
    }

    String firstLineText = ((TextComponent) lines.get(0)).content();

    if (firstLineText.equalsIgnoreCase("[WonderQuests]")) {

      if (lines.get(1) == null) {
        return;
      }

      String pluginSignType = ((TextComponent) lines.get(1)).content();

      if (pluginSignType.equalsIgnoreCase("active-quests")) {

        Player player = event.getPlayer();
        Sign sign = (Sign) event.getBlock().getState();

        // TODO: Store the sign in the database so we can update it later

//        activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId())
//            .thenAccept(activeQuests -> {
//              activeQuests.stream().min((a, b) -> b.getStartedAt().compareTo(a.getStartedAt()))
//                  .ifPresentOrElse(activeQuest -> {
//
//                    Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("WonderQuests"), () -> {
//                      player.sendSignChange(sign.getLocation(), new String[] {
//                          ChatColor.BOLD + "" + ChatColor.GREEN + "WonderQuests",
//                          ChatColor.YELLOW + activeQuest.getQuestDetails().getName(),
//                          ChatColor.GREEN + String.valueOf(activeQuest.getProgressPercentage()) + "%",
//                          ChatColor.GRAY + String.valueOf(activeQuest.getProgress()) + "/" + activeQuest.getTarget()
//                      });
//                    }, 20L);
//
//                  }, () -> {
//
//                    Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("WonderQuests"), () -> {
//                      player.sendSignChange(sign.getLocation(), new String[] {
//                          ChatColor.BOLD + "" + ChatColor.GREEN + "WonderQuests",
//                          null,
//                          ChatColor.GRAY + "No active quests",
//                          null
//                      });
//                    }, 20L);
//
//                  });
//            });

      }

    }

  }

}
