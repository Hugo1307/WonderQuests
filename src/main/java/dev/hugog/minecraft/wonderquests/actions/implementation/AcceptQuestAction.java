package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import dev.hugog.minecraft.wonderquests.mediators.QuestsMediator;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AcceptQuestAction extends AbstractAction<CompletableFuture<Boolean>> {

  private final ItemStack questItem;

  private final Logger logger;
  private final Messaging messaging;
  private final ActiveQuestsService activeQuestsService;
  private final QuestsService questsService;
  private final QuestsMediator questsMediator;
  private final ConcurrencyHandler concurrencyHandler;
  private final WonderQuests plugin;

  @Inject
  public AcceptQuestAction(@Assisted CommandSender sender, @Assisted ItemStack questItem,
      @Named("bukkitLogger") Logger logger,
      Messaging messaging,
      ActiveQuestsService activeQuestsService, QuestsService questsService,
      QuestsMediator questsMediator,
      ConcurrencyHandler concurrencyHandler, WonderQuests plugin) {

    super(sender);
    this.questItem = questItem;
    this.logger = logger;
    this.messaging = messaging;
    this.activeQuestsService = activeQuestsService;
    this.questsService = questsService;
    this.questsMediator = questsMediator;
    this.concurrencyHandler = concurrencyHandler;
    this.plugin = plugin;

  }

  @Override
  public CompletableFuture<Boolean> execute() {

    if (!(sender instanceof Player player)) {
      return CompletableFuture.completedFuture(false);
    }

    PersistentDataContainer itemPersistentContainer = questItem.getItemMeta()
        .getPersistentDataContainer();

    // Same namespace as the one in AvailableQuestsGui class
    NamespacedKey namespacedKey = new NamespacedKey(plugin, "quest_id");

    // Not a quest item
    if (!itemPersistentContainer.has(namespacedKey, PersistentDataType.INTEGER)) {
      return CompletableFuture.completedFuture(false);
    }

    Integer questId = itemPersistentContainer.get(namespacedKey, PersistentDataType.INTEGER);

    return activeQuestsService.hasAlreadyStartedQuest(player.getUniqueId(), questId)
        .thenCompose((alreadyStarted) -> {

          if (alreadyStarted) {
            player.sendMessage(
                messaging.getLocalizedChatWithPrefix("actions.quests.accept.already_started")
            );
            return CompletableFuture.completedFuture(false);
          }

          return questsService.getQuestById(questId)
              .thenCompose((quest) -> {

                if (quest.isEmpty()) {
                  player.sendMessage(
                      messaging.getLocalizedChatWithPrefix("actions.quests.accept.not_found")
                  );
                  return CompletableFuture.completedFuture(false);
                }

                // Check Requirements
                boolean hasNecessary = questsMediator.playerHasRequirements(player, quest.get());

                if (!hasNecessary) {
                  player.sendMessage(
                      messaging.getLocalizedChatWithPrefix(
                          "actions.quests.accept.requirements.lack"
                      )
                  );
                  return CompletableFuture.completedFuture(false);
                }

                return activeQuestsService.startQuest(
                        player.getUniqueId(),
                        questId,
                        quest.get().getObjective().getNumericValue())
                    .thenApply((success) -> {

                      if (!success) {
                        player.sendMessage(messaging.getLocalizedChatWithPrefix(
                            "actions.quests.accept.error"
                        ));
                        logger.warning(
                            "Unable to start quest " + questId + " for player " + player.getName()
                                + " - database error.");
                        return false;
                      }

                      concurrencyHandler.run(() -> plugin.getServer().getPluginManager()
                              .callEvent(
                                  new ActiveQuestUpdateEvent(player, QuestUpdateType.STARTED, null)),
                          true);

                      player.sendMessage(messaging.getLocalizedChatWithPrefix(
                          "actions.quests.accept.success",
                          Component.text(quest.get().getName())
                      ));

                      player.sendMessage(messaging.getQuestMessagePrefix()
                          .append(Component.text(quest.get().getOpeningMsg())));

                      return true;

                    })
                    .exceptionally((throwable) -> {
                      player.sendMessage(messaging.getLocalizedChatWithPrefix(
                          "actions.quests.accept.error"
                      ));
                      logger.warning(
                          "Unable to start quest " + questId + " for player " + player.getName()
                              + "- Exception: " + throwable.getMessage());
                      return false;
                    });


              }).exceptionally((throwable) -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.quests.accept.error"
                ));
                logger.warning(
                    "Unable to start quest " + questId + " for player " + player.getName()
                        + "- Exception: " + throwable.getMessage());
                return false;
              });

        })
        .exceptionally((throwable) -> {
          player.sendMessage(messaging.getLocalizedChatWithPrefix(
              "actions.quests.accept.error"
          ));
          logger.warning(
              "Unable to start quest " + questId + " for player " + player.getName()
                  + "- Exception: " + throwable.getMessage());
          return false;
        });

  }
}
