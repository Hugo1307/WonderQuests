package dev.hugog.minecraft.wonderquests.actions.implementation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import dev.hugog.minecraft.wonderquests.actions.AbstractAction;
import dev.hugog.minecraft.wonderquests.data.services.QuestRequirementsService;
import dev.hugog.minecraft.wonderquests.language.Messaging;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteRequirementAction extends AbstractAction<CompletableFuture<Boolean>> {

  private final int requirementId;
  private final Messaging messaging;
  private final QuestRequirementsService questRequirementsService;

  @Inject
  public DeleteRequirementAction(@Assisted CommandSender sender, @Assisted int requirementId,
      Messaging messaging, QuestRequirementsService questRequirementsService) {

    super(sender);
    this.requirementId = requirementId;
    this.messaging = messaging;
    this.questRequirementsService = questRequirementsService;

  }

  @Override
  public CompletableFuture<Boolean> execute() {

    if (!(sender instanceof Player player)) {
      return CompletableFuture.completedFuture(false);
    }

    return questRequirementsService.checkIfRequirementExists(requirementId)
        .thenCompose(exists -> {

          if (!exists) {
            player.sendMessage(messaging.getLocalizedChatWithPrefix(
                "actions.requirements.delete.not_found",
                Component.text(requirementId)
            ));
            return CompletableFuture.completedFuture(false);
          }

          return questRequirementsService.deleteRequirement(requirementId)
              .thenApply(ignored -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.requirements.delete.success",
                    Component.text(requirementId)
                ));
                return true;
              })
              .exceptionally(deleteThrowable -> {
                player.sendMessage(messaging.getLocalizedChatWithPrefix(
                    "actions.requirements.delete.error",
                    Component.text(requirementId)
                ));
                return false;
              });

        })
        .exceptionally(throwable -> {
          player.sendMessage(messaging.getLocalizedChatWithPrefix(
              "actions.requirements.delete.error",
              Component.text(requirementId)
          ));
          return false;
        });

  }

}
