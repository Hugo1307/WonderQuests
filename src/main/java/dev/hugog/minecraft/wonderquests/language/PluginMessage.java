package dev.hugog.minecraft.wonderquests.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PluginMessage {

  QUEST_CREATION_CMD_USAGE("commands.quest.create.usage");

  private final String key;

}
