package dev.hugog.minecraft.wonderquests.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import dev.hugog.minecraft.wonderquests.data.repositories.AbstractDataRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.PlayersRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestObjectivesRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRequirementsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestRewardsRepository;
import dev.hugog.minecraft.wonderquests.data.repositories.QuestsRepository;
import java.util.List;
import java.util.logging.Logger;

public class BasicBinderModule extends AbstractModule {

  private final WonderQuests plugin;

  public BasicBinderModule(WonderQuests plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void configure() {

    this.bind(WonderQuests.class).toInstance(plugin);
    this.bind(Logger.class).annotatedWith(Names.named("bukkitLogger"))
        .toInstance(plugin.getLogger());

  }

  @Singleton
  @Provides
  @Inject
  public List<AbstractDataRepository<?,?>> dataRepositories(
      PlayersRepository playersRepository,
      QuestsRepository questsRepository, QuestObjectivesRepository questObjectivesRepository,
      QuestRequirementsRepository questRequirementsRepository,
      QuestRewardsRepository questRewardsRepository
  ) {
    return List.of(
        playersRepository,
        questsRepository,
        questObjectivesRepository,
        questRequirementsRepository,
        questRewardsRepository
    );

  }

}
