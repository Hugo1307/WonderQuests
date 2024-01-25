package dev.hugog.minecraft.wonderquests.mediators;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.CompletedQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.CompletedQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

public class QuestsMediator {

  private final Logger logger;
  private final ConcurrencyHandler concurrencyHandler;
  private final QuestsService questsService;
  private final ActiveQuestsService activeQuestsService;
  private final CompletedQuestsService completedQuestsService;
  private final WonderQuests plugin;
  private final Messaging messaging;

  @Inject
  public QuestsMediator(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, QuestsService questsService,
      ActiveQuestsService activeQuestsService, CompletedQuestsService completedQuestsService,
      WonderQuests plugin, Messaging messaging) {
    this.logger = logger;
    this.concurrencyHandler = concurrencyHandler;
    this.questsService = questsService;
    this.activeQuestsService = activeQuestsService;
    this.completedQuestsService = completedQuestsService;
    this.plugin = plugin;
    this.messaging = messaging;
  }

  public void giveQuestRewardsToPlayer(Player player, Integer questId) {

    // TODO: Add messages to the player when the rewards are granted
    questsService.getQuestById(questId).thenAccept(questOptional -> {

      if (questOptional.isEmpty()) {
        logger.info(
            "Unable to grant rewards to player " + player.getName() + " for quest " + questId
                + " - quest not found.");
        return;
      }

      QuestDto quest = questOptional.get();

      quest.getRewards().forEach(reward -> {

        switch (reward.getType()) {
          case EXPERIENCE -> player.giveExp(reward.getNumericValue().intValue());
          case MONEY, ITEMS -> {
          }
          case COMMAND -> concurrencyHandler.runOnMainThread(
              () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                  reward.getStringValue().replace("%player%", player.getName())
              ));
        }
      });

    });

  }

  public void updateQuestProgress(Player player, ActiveQuestDto activeQuest) {

    notifyQuestUpdate(player, activeQuest);
    activeQuestsService.incrementQuestProgress(player.getUniqueId(), activeQuest.getQuestId());

  }

  public void notifyQuestUpdate(Player player, ActiveQuestDto activeQuest) {

    // Call the ActiveQuestUpdateEvent to update the signs, for example.
    concurrencyHandler.run(
        () -> plugin.getServer().getPluginManager().callEvent(
            new ActiveQuestUpdateEvent(player, QuestUpdateType.UPDATED, activeQuest)
        ),
        true
    );

  }

  public void handleQuestCompletion(Player player, ActiveQuestDto activeQuest) {

    activeQuestsService.removeQuest(new PlayerQuestKey(player.getUniqueId(), activeQuest.getQuestId()))
        .thenRun(() -> {

          // Call the ActiveQuestUpdateEvent to update the signs, for example.
          concurrencyHandler.run(() -> plugin.getServer().getPluginManager()
                  .callEvent(
                      new ActiveQuestUpdateEvent(player, QuestUpdateType.COMPLETED, activeQuest)),
              true);

          giveQuestRewardsToPlayer(player, activeQuest.getQuestId());

          completedQuestsService.addCompletedQuest(new CompletedQuestDto(
              player.getUniqueId(),
              activeQuest.getQuestId()
          ));

          questsService.getQuestById(activeQuest.getQuestId()).thenAccept((quest) -> {

            // The quest doesn't exist
            if (quest.isEmpty()) {
              return;
            }

            QuestDto questDto = quest.get();

            player.sendMessage(messaging.getLocalizedChatWithPrefix(
                "quests.completion.message",
                Component.text(questDto.getName()))
            );

            player.showTitle(Title.title(
                    messaging.getLocalizedRawMessage("quests.completion.message.title")
                        .color(NamedTextColor.GOLD),
                    messaging.getLocalizedRawMessage(
                            "quests.completion.message.subtitle",
                            Component.text(questDto.getName(), NamedTextColor.GREEN)
                        )
                        .color(NamedTextColor.GRAY)
                )
            );

            player.sendMessage(messaging.getQuestMessagePrefix()
                .append(Component.text(quest.get().getClosingMsg())));

          });

        });

  }

}
