package dev.hugog.minecraft.wonderquests.chat.summaries;

import org.bukkit.entity.Player;

public interface PluginChatSummary {

  void showToPlayer(Player player, Object... args);

}
