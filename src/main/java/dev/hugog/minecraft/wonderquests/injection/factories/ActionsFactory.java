package dev.hugog.minecraft.wonderquests.injection.factories;

import dev.hugog.minecraft.wonderquests.actions.concrete.CreateQuestAction;
import dev.hugog.minecraft.wonderquests.actions.concrete.CreateQuestRequirementAction;
import dev.hugog.minecraft.wonderquests.actions.concrete.ShowAvailableQuestsAction;
import org.bukkit.command.CommandSender;

public interface ActionsFactory {

  CreateQuestAction buildCreateQuestAction(CommandSender sender);

  CreateQuestRequirementAction buildCreateQuestRequirementAction(CommandSender sender, int questId);

  ShowAvailableQuestsAction buildShowAvailableQuestsAction(CommandSender sender);

}
