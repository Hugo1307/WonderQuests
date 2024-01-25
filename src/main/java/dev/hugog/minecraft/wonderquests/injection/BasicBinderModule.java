package dev.hugog.minecraft.wonderquests.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.data.repositories.AbstractDataRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.ActiveQuestRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.CompletedQuestRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestObjectivesRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.SignsRepository;
import dev.hugog.minecraft.wonderquests.injection.factories.ActionsFactory;
import dev.hugog.minecraft.wonderquests.injection.factories.GuiFactory;
import java.io.File;
import org.bukkit.Server;

import java.util.List;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;

public class BasicBinderModule extends AbstractModule {

  private final WonderQuests plugin;

  public BasicBinderModule(WonderQuests plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void configure() {

    FactoryModuleBuilder factoryModuleBuilder = new FactoryModuleBuilder();
    install(factoryModuleBuilder.build(ActionsFactory.class));
    install(factoryModuleBuilder.build(GuiFactory.class));

    this.bind(WonderQuests.class)
        .toInstance(plugin);

    this.bind(Logger.class).annotatedWith(Names.named("bukkitLogger"))
        .toInstance(plugin.getLogger());

    this.bind(FileConfiguration.class)
        .toInstance(plugin.getConfig());

    this.bind(File.class).annotatedWith(Names.named("pluginFolder"))
        .toInstance(plugin.getDataFolder());

    this.bind(Server.class).toInstance(plugin.getServer());

  }

  @Singleton
  @Provides
  @Inject
  public List<AbstractDataRepository<?, ?>> dataRepositories(
      PlayersRepository playersRepository,
      QuestsRepository questsRepository, QuestObjectivesRepository questObjectivesRepository,
      QuestRequirementsRepository questRequirementsRepository,
      QuestRewardsRepository questRewardsRepository,
      ActiveQuestRepository activeQuestRepository,
      CompletedQuestRepository completedQuestRepository,
      SignsRepository signsRepository
  ) {
    return List.of(
        playersRepository,
        questsRepository,
        questObjectivesRepository,
        questRequirementsRepository,
        questRewardsRepository,
        activeQuestRepository,
        completedQuestRepository,
        signsRepository
    );

  }

}
