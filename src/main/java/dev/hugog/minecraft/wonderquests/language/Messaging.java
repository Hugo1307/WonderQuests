package dev.hugog.minecraft.wonderquests.language;

import com.google.inject.Singleton;
import java.util.Locale;
import java.util.ResourceBundle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;

@Singleton
public class Messaging {

  private final TranslationRegistry translationRegistry;

  public Messaging() {
    this.translationRegistry = TranslationRegistry.create(Key.key("wonder_quests"));
  }

  public void loadBundles() {

    ResourceBundle englishBundle = ResourceBundle.getBundle(
        "lang.LanguageBundle",
        Locale.forLanguageTag("en-US"),
        UTF8ResourceBundleControl.get());

    ResourceBundle portugueseBundle = ResourceBundle.getBundle(
        "lang.LanguageBundle",
        Locale.forLanguageTag("pt-PT"),
        UTF8ResourceBundleControl.get());

    translationRegistry.registerAll(Locale.forLanguageTag("en-US"), englishBundle, true);
    translationRegistry.registerAll(Locale.forLanguageTag("pt-PT"), portugueseBundle, true);

    GlobalTranslator.translator().addSource(translationRegistry);

  }

  public Component getLocalizedChatWithPrefix(String key, Component... args) {
    return Component.text()
        .append(Component.text("[", NamedTextColor.GRAY))
        .append(Component.text("WonderQuests", NamedTextColor.GREEN))
        .append(Component.text("] ", NamedTextColor.GRAY))
        .color(NamedTextColor.GRAY)
        .append(Component.translatable(key, args))
        .build();
  }

  public Component getLocalizedChatNoPrefix(String key, Component... args) {
    return Component.text()
        .color(NamedTextColor.GRAY)
        .append(Component.translatable(key, args))
        .build();
  }

  public Component getLocalizedChatInfo(String key, Component... args) {
    return Component.text()
        .color(NamedTextColor.GREEN)
        .content("[!] ")
        .color(NamedTextColor.GRAY)
        .append(Component.translatable(key, args))
        .build();
  }

  public Component getChatSeparator() {
    return Component.text()
        .color(NamedTextColor.GRAY)
        .content("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=")
        .build();
  }

  public Component getLocalizedRawMessage(String key, Component... args) {
    return Component.translatable(key, args);
  }

  public Component getLocalizedRawMessage(PluginMessage key, Component... args) {
    return Component.translatable(key.getKey(), args);
  }

}
