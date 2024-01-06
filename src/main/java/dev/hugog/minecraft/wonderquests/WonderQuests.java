package dev.hugog.minecraft.wonderquests;

import com.google.inject.Injector;
import dev.hugog.minecraft.wonderquests.injection.BasicBinderModule;
import org.bukkit.plugin.java.JavaPlugin;

public final class WonderQuests extends JavaPlugin {

  @Override
  public void onEnable() {

    initDependencyInjectionModule();

    getLogger().info("Plugin successfully enabled!");

  }

  @Override
  public void onDisable() {
    getLogger().info("Plugin successfully disabled!");
  }

  private void initDependencyInjectionModule() {
    BasicBinderModule guiceBinderModule = new BasicBinderModule(this);
    Injector injector = guiceBinderModule.createInjector();
    injector.injectMembers(this);
  }

}
