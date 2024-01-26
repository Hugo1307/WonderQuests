package dev.hugog.minecraft.wonderquests.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.hugog.minecraft.wonderquests.WonderQuests;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Singleton
public class ConcurrencyHandler {

  private final Executor executorPool;

  private final WonderQuests plugin;

  @Inject
  public ConcurrencyHandler(WonderQuests plugin) {

    ThreadFactory threadFactory = new ThreadFactoryBuilder()
        .setNameFormat("WonderQuests-Thread-%d")
        .build();

    this.executorPool = Executors.newFixedThreadPool(5, threadFactory);
    this.plugin = plugin;

  }

  public ConcurrencyHandler() {
    this(null);
  }

  public CompletableFuture<Void> run(Runnable runnable, boolean async) {

    if (async) {
      return CompletableFuture.runAsync(runnable, executorPool);
    } else {
      runnable.run();
      return CompletableFuture.completedFuture(null);
    }

  }

  public CompletableFuture<Void> runAfterMultiple(CompletableFuture<?>[] completableFutures,
      Runnable runnable, boolean async) {

    if (async) {
      return CompletableFuture.allOf(completableFutures).thenRunAsync(runnable, executorPool);
    } else {
      Arrays.stream(completableFutures).forEach(CompletableFuture::join);
      runnable.run();
      return CompletableFuture.completedFuture(null);
    }

  }

  public CompletableFuture<Void> runDelayed(Runnable runnable, long delay, TimeUnit timeUnit,
      boolean async) {

    Executor delayedExecutor = CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS);

    if (async) {
      return CompletableFuture.runAsync(runnable, delayedExecutor);
    } else {
      delayedExecutor.execute(runnable);
      return CompletableFuture.completedFuture(null);
    }

  }

  public <T> CompletableFuture<T> supply(Supplier<T> supplier, boolean async) {

    if (async) {
      return CompletableFuture.supplyAsync(supplier, executorPool);
    } else {
      return CompletableFuture.completedFuture(supplier.get());
    }

  }

  public <T> CompletableFuture<Void> thenAccept(CompletableFuture<T> completableFuture,
      Consumer<? super T> consumer, boolean async) {

    if (async) {
      return completableFuture.thenAcceptAsync(consumer, executorPool);
    } else {
      consumer.accept(completableFuture.join());
      return CompletableFuture.completedFuture(null);
    }

  }

  public CompletableFuture<?>[] getListOfFutures(CompletableFuture<?>... futures) {
    return futures;
  }

  public void runOnMainThread(Runnable runnable) {
    plugin.getServer().getScheduler().runTask(plugin, runnable);
  }

}
