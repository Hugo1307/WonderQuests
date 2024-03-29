package dev.hugog.minecraft.wonderquests.mediators;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.concurrency.ConcurrencyHandler;
import dev.hugog.minecraft.wonderquests.data.dtos.ActiveQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.CompletedQuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestDto;
import dev.hugog.minecraft.wonderquests.data.dtos.QuestRequirementDto;
import dev.hugog.minecraft.wonderquests.data.keys.PlayerQuestKey;
import dev.hugog.minecraft.wonderquests.data.services.ActiveQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.CompletedQuestsService;
import dev.hugog.minecraft.wonderquests.data.services.QuestsService;
import dev.hugog.minecraft.wonderquests.events.ActiveQuestUpdateEvent;
import dev.hugog.minecraft.wonderquests.events.QuestUpdateType;
import dev.hugog.minecraft.wonderquests.hooks.EconomyHook;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This class mediates between the quest services and the player.
 */
public class QuestsMediator {

  private final Logger logger;
  private final ConcurrencyHandler concurrencyHandler;
  private final QuestsService questsService;
  private final ActiveQuestsService activeQuestsService;
  private final CompletedQuestsService completedQuestsService;
  private final WonderQuests plugin;
  private final Messaging messaging;
  private final EconomyHook economyHook;

  /**
   * Constructor for the QuestsMediator class.
   *
   * @param logger The logger instance.
   * @param concurrencyHandler The handler for concurrency.
   * @param questsService The service for quests.
   * @param activeQuestsService The service for active quests.
   * @param completedQuestsService The service for completed quests.
   * @param plugin The plugin instance.
   * @param messaging The messaging instance used for sending messages.
   * @param economyHook The economy hook instance.
   */
  @Inject
  public QuestsMediator(@Named("bukkitLogger") Logger logger,
      ConcurrencyHandler concurrencyHandler, QuestsService questsService,
      ActiveQuestsService activeQuestsService, CompletedQuestsService completedQuestsService,
      WonderQuests plugin, Messaging messaging, EconomyHook economyHook) {

    this.logger = logger;
    this.concurrencyHandler = concurrencyHandler;
    this.questsService = questsService;
    this.activeQuestsService = activeQuestsService;
    this.completedQuestsService = completedQuestsService;
    this.plugin = plugin;
    this.messaging = messaging;
    this.economyHook = economyHook;

  }

  /**
   * This method gives quest rewards to the player.
   *
   * @param player The player to give the rewards to.
   * @param questId The id of the quest.
   */
  public void giveQuestRewardsToPlayer(Player player, Integer questId) {

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
          case MONEY -> economyHook.depositPlayer(player.getUniqueId(), reward.getNumericValue());
          case ITEMS -> player.getInventory().addItem(
              new ItemStack(
                  Objects.requireNonNull(Material.matchMaterial(reward.getStringValue())),
                  reward.getNumericValue().intValue()
              )
          );
          case COMMAND -> concurrencyHandler.runOnMainThread(
              () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                  reward.getStringValue().replace("%player%", player.getName())
              ));
        }
      });

      // Play a sound to the player
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

    });

  }

  /**
   * This method updates the quest progress.
   *
   * @param player The player whose quest progress is to be updated.
   * @param activeQuest The active quest to be updated.
   */
  public void updateQuestProgress(Player player, ActiveQuestDto activeQuest) {

    notifyQuestUpdate(player, activeQuest);
    activeQuestsService.incrementQuestProgress(player.getUniqueId(), activeQuest.getQuestId());

  }

  /**
   * This method notifies the quest update.
   *
   * @param player The player to be notified.
   * @param activeQuest The active quest to be notified.
   */
  public void notifyQuestUpdate(Player player, ActiveQuestDto activeQuest) {

    // Call the ActiveQuestUpdateEvent to update the signs, for example.
    concurrencyHandler.run(
        () -> plugin.getServer().getPluginManager().callEvent(
            new ActiveQuestUpdateEvent(player, QuestUpdateType.UPDATED, activeQuest)
        ),
        true
    );

  }

  /**
   * This method gets the potential available quests for the player.
   *
   * @param player The player to get the potential available quests for.
   * @return A CompletableFuture that contains a set of potential available quests.
   */
  public CompletableFuture<Set<QuestDto>> getPotentialAvailableQuests(Player player) {
    return questsService.getAllQuests().thenApply(quests -> quests.stream()
        .filter(quest -> playerHasRequirements(player, quest))
        .collect(Collectors.toSet()));
  }

  /**
   * This method gets the available quests for the player.
   *
   * @param player The player to get the available quests for.
   * @return A CompletableFuture that contains a set of available quests.
   */
  public CompletableFuture<Set<QuestDto>> getAvailableQuests(Player player) {

    return getPotentialAvailableQuests(player)
        .thenCompose(
            quests -> completedQuestsService.getCompletedQuestByPlayer(player.getUniqueId())
                .thenApply(questsCompleted -> {

                  Set<QuestDto> availableQuests = new HashSet<>();
                  Set<Integer> completedQuestsIds = questsCompleted.stream()
                      .map(CompletedQuestDto::getQuestId)
                      .collect(Collectors.toSet());

                  quests.forEach(quest -> {
                    if (!completedQuestsIds.contains(quest.getId())) {
                      availableQuests.add(quest);
                    }
                  });

                  return availableQuests;

                }));

  }

  /**
   * This method checks if the player has the requirements for the quest.
   *
   * @param player The player to check the requirements for.
   * @param quest The quest to check the requirements of.
   * @return A boolean indicating if the player has the requirements for the quest.
   */
  public boolean playerHasRequirements(Player player, QuestDto quest) {

    boolean hasRequirements = true;

    List<QuestRequirementDto> questRequirements = quest.getRequirements();

    for (QuestRequirementDto requirement : questRequirements) {

      hasRequirements = switch (requirement.getType()) {
        case MONEY -> hasRequirements
            && economyHook.getBalance(player.getUniqueId()) >= requirement.getNumericValue();
        case EXPERIENCE -> hasRequirements
            && player.getLevel() >= requirement.getNumericValue().intValue();
        case PERMISSION -> hasRequirements && player.hasPermission(requirement.getStringValue());
        case ITEM -> hasRequirements && player.getInventory().containsAtLeast(
            new ItemStack(
                Objects.requireNonNull(Material.matchMaterial(requirement.getStringValue()))
            ),
            1
        );
      };

    }

    return hasRequirements;

  }

  /**
   * This method handles the quest completion.
   *
   * @param player The player who completed the quest.
   * @param activeQuest The active quest that was completed.
   */
  public void handleQuestCompletion(Player player, ActiveQuestDto activeQuest) {

    activeQuestsService.removeQuest(
            new PlayerQuestKey(player.getUniqueId(), activeQuest.getQuestId()))
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