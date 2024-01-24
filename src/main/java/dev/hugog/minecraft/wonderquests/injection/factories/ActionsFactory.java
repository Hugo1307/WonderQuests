package dev.hugog.minecraft.wonderquests.injection.factories;

import dev.hugog.minecraft.wonderquests.actions.implementation.CancelQuestAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestObjectiveAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestRequirementAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.CreateQuestRewardAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowActiveQuestsAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowAvailableQuestsAction;
import dev.hugog.minecraft.wonderquests.actions.implementation.ShowQuestDetailsAction;
import org.bukkit.command.CommandSender;

public interface ActionsFactory {

  CreateQuestAction buildCreateQuestAction(CommandSender sender);

  CreateQuestRequirementAction buildCreateQuestRequirementAction(CommandSender sender, int questId);

  CreateQuestObjectiveAction buildCreateQuestObjectiveAction(CommandSender sender, int questId);

  CreateQuestRewardAction buildCreateQuestRewardAction(CommandSender sender, int questId);

  ShowAvailableQuestsAction buildShowAvailableQuestsAction(CommandSender sender);

  ShowActiveQuestsAction buildShowActiveQuestsAction(CommandSender sender);

  ShowQuestDetailsAction buildShowQuestDetailsAction(CommandSender sender, int questId);

  CancelQuestAction buildCancelQuestAction(CommandSender sender, int questId);

}
