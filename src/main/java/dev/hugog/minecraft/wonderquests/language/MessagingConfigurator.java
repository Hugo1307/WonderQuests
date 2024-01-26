package dev.hugog.minecraft.wonderquests.language;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class MessagingConfigurator {

  private final Logger logger;
  private final File pluginFolder;

  @Inject
  public MessagingConfigurator(@Named("bukkitLogger") Logger logger,
      @Named("pluginFolder") File pluginFolder) {

    this.logger = logger;
    this.pluginFolder = pluginFolder;

  }

  public void configure() {

    File langFolder = createLanguageDirectoryIfNotExists();

    List<File> languageFiles = List.of(
        new File(langFolder, "languageBundle.properties"),
        new File(langFolder, "languageBundle_en_US.properties"),
        new File(langFolder, "languageBundle_pt_PT.properties"),
        new File(langFolder, "languageBundle_de.properties")
    );

    List<Boolean> languageFilesCreated = languageFiles.stream()
        .map(this::createLanguageFileIfNotExists)
        .toList();

    List<String> languageResourceFiles = List.of(
        "lang" + File.separator + "languageBundle.properties",
        "lang" + File.separator + "languageBundle_en_US.properties",
        "lang" + File.separator + "languageBundle_pt_PT.properties",
        "lang" + File.separator + "languageBundle_de.properties"
    );

    List<InputStream> inputStreams = languageResourceFiles.stream()
        .map(this::getResourceAsStream)
        .toList();

    for (int i = 0; i < languageFilesCreated.size(); i++) {
      if (languageFilesCreated.get(i)) {
        logger.info("Created the " + languageFiles.get(i).getName() + " language file!");
        writeInputStreamToFile(inputStreams.get(i), languageFiles.get(i).toPath());
      }
    }

  }

  private File createLanguageDirectoryIfNotExists() {

    File langFolder = new File(pluginFolder, "lang");

    if (!langFolder.exists()) {

      if (langFolder.mkdir()) {
        logger.info("Created plugin's language folder!");
        return langFolder;
      } else {
        logger.warning("Error while creating plugin's language folder!");
        return null;
      }

    }

    return langFolder;

  }

  private boolean createLanguageFileIfNotExists(File langFile) {

    if (!langFile.exists()) {
      try {
        return langFile.createNewFile();
      } catch (IOException e) {
        logger.severe(
            "Error while creating the " + langFile.getName() + " language file! Caused by: "
                + e.getMessage());
      }
    }

    return false;

  }

  private InputStream getResourceAsStream(String resourcePath) {
    return getClass().getClassLoader().getResourceAsStream(resourcePath);
  }

  private void writeInputStreamToFile(InputStream inputStream, Path destinationPath) {
    try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
      String inputStreamString = scanner.useDelimiter("\\A").next();
      Files.writeString(destinationPath, inputStreamString);
    } catch (IOException e) {
      logger.severe("Error while writing the language file! Caused by: " + e.getMessage());
    }
  }

}
