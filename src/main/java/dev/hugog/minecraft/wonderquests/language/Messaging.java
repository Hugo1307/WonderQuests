package dev.hugog.minecraft.wonderquests.language;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;

@Singleton
public class Messaging {

  private final TranslationRegistry translationRegistry;

  private final File pluginFolder;

  @Inject
  public Messaging(@Named("pluginFolder") File pluginFolder) {
    this.translationRegistry = TranslationRegistry.create(Key.key("wonder_quests"));
    this.pluginFolder = pluginFolder;
  }

  public void loadBundles() {

    File langFolder = new File(pluginFolder, "lang");
    URL[] urls;
    try {
      urls = new URL[]{langFolder.toURI().toURL()};
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

    try (URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader())) {

      ResourceBundle englishBundle = ResourceBundle.getBundle(
          "languageBundle",
          Locale.forLanguageTag("en-US"),
          classLoader);

      ResourceBundle portugueseBundle = ResourceBundle.getBundle(
          "languageBundle",
          Locale.forLanguageTag("pt-PT"),
          classLoader);

      ResourceBundle deutscheBundle = ResourceBundle.getBundle(
          "languageBundle",
          Locale.forLanguageTag("de"),
          classLoader);

      translationRegistry.registerAll(Locale.forLanguageTag("en-US"), englishBundle, true);
      translationRegistry.registerAll(Locale.forLanguageTag("pt-PT"), portugueseBundle, true);
      translationRegistry.registerAll(Locale.forLanguageTag("de"), deutscheBundle, true);

      GlobalTranslator.translator().addSource(translationRegistry);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

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

  public Component getQuestMessagePrefix() {
    return Component.text()
        .append(Component.text(" >>> ", NamedTextColor.YELLOW))
        .color(NamedTextColor.GRAY)
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

}
