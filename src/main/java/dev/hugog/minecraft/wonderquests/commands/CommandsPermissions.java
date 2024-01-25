package dev.hugog.minecraft.wonderquests.commands;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandsPermissions {

  ABORT_QUEST("wonderquests.commands.quest.abort"),
  ACTIVE_QUESTS("wonderquests.commands.quest.active"),
  AVAILABLE_QUESTS("wonderquests.commands.quest.available"),
  CREATE_QUEST("wonderquests.commands.quest.create"),
  CREATE_REQUIREMENT("wonderquests.commands.requirement.create"),
  CREATE_REWARD("wonderquests.commands.reward.create"),
  DELETE_QUEST("wonderquests.commands.quest.delete"),
  DELETE_REQUIREMENT("wonderquests.commands.requirement.delete"),
  DELETE_REWARD("wonderquests.commands.reward.delete"),
  LIST_QUESTS("wonderquests.commands.quest.list"),
  QUEST_DETAILS("wonderquests.commands.quest.details"),
  ADMIN("wonderquests.commands.admin");

  private final String permission;

}
