package dev.hugog.minecraft.wonderquests.listeners;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.services.SignService;
import dev.hugog.minecraft.wonderquests.data.types.SignType;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignUpdateListener implements Listener {

  private final SignService signService;
  private final Messaging messaging;

  @Inject
  public SignUpdateListener(SignService signService, Messaging messaging) {
    this.signService = signService;
    this.messaging = messaging;
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

      if (pluginSignType.equalsIgnoreCase("active_quest")) {

        Sign sign = (Sign) event.getBlock().getState();
        Player player = event.getPlayer();

        player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.creation.scheduled"));

        signService.registerSign(SignType.ACTIVE_QUEST, sign.getLocation()).thenAccept(id -> {
          if (id == null) {
            player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.creation.failed"));
            return;
          }
          player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.creation.successful"));
        }).exceptionally(throwable -> {
          player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.creation.failed"));
          return null;
        });

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

  @EventHandler
  public void onSignDestroyed(BlockBreakEvent event) {

    Block destroyedBlock = event.getBlock();
    Player player = event.getPlayer();

    if (destroyedBlock.getState() instanceof Sign) {

      Sign sign = (Sign) event.getBlock().getState();
      SignSide signSide = sign.getSide(Side.FRONT);

      Component firstLine = signSide.lines().get(0);

      if (firstLine == null) {
        return;
      }

      String firstLineText = ((TextComponent) firstLine).content();

      // If the sign is not a WonderQuests sign, ignore
      if (!firstLineText.contains("WonderQuests")) {
        return;
      }

      player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.deletion.scheduled"));
      signService.unregisterSign(sign.getLocation())
          .thenAccept(unregisteredId -> {
            if (unregisteredId != null) {
              player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.deletion.successful"));
            } else {
              player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.deletion.failed"));
            }
          }).exceptionally(throwable -> {
            player.sendMessage(messaging.getLocalizedChatWithPrefix("signs.deletion.failed"));
            return null;
          });

    }

  }

}
