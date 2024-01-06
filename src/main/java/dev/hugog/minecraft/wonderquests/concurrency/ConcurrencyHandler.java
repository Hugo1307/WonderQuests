package dev.hugog.minecraft.wonderquests.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Singleton
public class ConcurrencyHandler {

  public Executor executorPool;

  public ConcurrencyHandler() {

    ThreadFactory threadFactory = new ThreadFactoryBuilder()
        .setNameFormat("WonderQuests-Thread-%d")
        .build();

    this.executorPool = Executors.newFixedThreadPool(5, threadFactory);

  }

  public CompletableFuture<Void> run(Runnable runnable, boolean async) {

    if (async) {
      return CompletableFuture.runAsync(runnable, executorPool);
    } else {
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

}
