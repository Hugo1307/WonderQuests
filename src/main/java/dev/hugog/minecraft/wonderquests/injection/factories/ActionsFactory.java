package dev.hugog.minecraft.wonderquests.injection.factories;

import dev.hugog.minecraft.wonderquests.actions.implementation.CancelQuestAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestRequirementAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestRewardAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.DeleteQuestAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.DeleteRequirementAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowActiveQuestsAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowAllQuestsAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowAvailableQuestsAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowQuestDetailsAction;
import org.bukkit.command.CommandSender;

public interface ActionsFactory {

  CreateQuestAction buildCreateQuestAction(CommandSender sender);

  CreateQuestRequirementAction buildCreateQuestRequirementAction(CommandSender sender, int questId);

  CreateQuestRewardAction buildCreateQuestRewardAction(CommandSender sender, int questId);

  ShowAvailableQuestsAction buildShowAvailableQuestsAction(CommandSender sender);

  ShowActiveQuestsAction buildShowActiveQuestsAction(CommandSender sender);

  ShowQuestDetailsAction buildShowQuestDetailsAction(CommandSender sender, int questId);

  ShowAllQuestsAction buildShowAllQuestsAction(CommandSender sender, int page);

  CancelQuestAction buildCancelQuestAction(CommandSender sender, int questId);

  DeleteQuestAction buildDeleteQuestAction(CommandSender sender, int questId);

  DeleteRequirementAction buildDeleteRequirementAction(CommandSender sender, int requirementId);

}
