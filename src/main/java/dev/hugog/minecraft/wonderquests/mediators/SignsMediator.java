package dev.hugog.minecraft.wonderquests.mediators;

import com.google.inject.Inject;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.SignService;
import dev.hugog.minecraft.wonderquests.data.types.SignType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SignsMediator {

  private final SignService signService;
  private final ActiveQuestsService activeQuestsService;

  @Inject
  public SignsMediator(SignService signService, ActiveQuestsService activeQuestsService) {
    this.signService = signService;
    this.activeQuestsService = activeQuestsService;
  }

  public void updateQuestsSign(Player player) {

    activeQuestsService.getActiveQuestsForPlayer(player.getUniqueId())
        .thenAccept(activeQuests -> {
          activeQuests.stream().min(
                  (a, b) -> b.getStartedAt().compareTo(a.getStartedAt())
              ) // Get the most recent quest
              .ifPresentOrElse(activeQuest -> updateQuestsSignUsingActiveQuest(player, activeQuest),
                  () -> updateQuestsSignUsingActiveQuest(player, null));
        });

  }

  @SuppressWarnings("deprecation")
  public void updateQuestsSignUsingActiveQuest(Player player, ActiveQuestDto activeQuest) {

    if (activeQuest != null) {
      signService.getAllSigns().thenAccept(signs -> {
        signs.stream()
            .filter(sign -> sign.getType() == SignType.ACTIVE_QUEST)
            .forEach(sign -> {

              World world = Bukkit.getWorld(sign.getWorldName());
              Location location = new Location(world, sign.getX(), sign.getY(),
                  sign.getZ());

              player.sendSignChange(location, new String[]{
                  LegacyComponentSerializer.legacySection().serialize(
                      Component.text("WonderQuests", NamedTextColor.GREEN)
                  ),
                  LegacyComponentSerializer.legacySection().serialize(
                      Component.text(
                          activeQuest.getQuestDetails().getName(),
                          NamedTextColor.BLUE
                      )
                  ),
                  LegacyComponentSerializer.legacySection().serialize(
                      Component.text(
                          activeQuest.getProgressPercentage() + "%",
                          NamedTextColor.YELLOW
                      )
                  ),
                  LegacyComponentSerializer.legacySection().serialize(
                      Component.text(
                          activeQuest.getProgress() + "/" + activeQuest.getTarget(),
                          NamedTextColor.GRAY
                      )
                  )
              });

            });
      });
    } else {

      signService.getAllSigns().thenAccept(signs -> {
        signs.stream()
            .filter(sign -> sign.getType() == SignType.ACTIVE_QUEST)
            .forEach(sign -> {

              World world = Bukkit.getWorld(sign.getWorldName());
              Location location = new Location(world, sign.getX(), sign.getY(),
                  sign.getZ());

              player.sendSignChange(location, new String[]{
                  LegacyComponentSerializer.legacySection().serialize(
                      Component.text("WonderQuests", NamedTextColor.GREEN)
                  ),
                  null,
                  LegacyComponentSerializer.legacySection().serialize(
                      Component.text(
                          "No active quests", // TODO: Add to messages
                          NamedTextColor.GRAY
                      )
                  ),
                  null
              });

            });
      });

    }

  }

}
