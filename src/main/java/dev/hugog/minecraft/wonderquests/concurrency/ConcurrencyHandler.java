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

/**
 * This class handles the concurrency in the application.
 */
@Singleton
public class ConcurrencyHandler {

  private final Executor executorPool;

  private final WonderQuests plugin;

  /**
   * Constructor for the ConcurrencyHandler class.
   *
   * @param plugin The plugin instance used for scheduling tasks.
   */
  @Inject
  public ConcurrencyHandler(WonderQuests plugin) {

    ThreadFactory threadFactory = new ThreadFactoryBuilder()
        .setNameFormat("WonderQuests-Thread-%d")
        .build();

    this.executorPool = Executors.newFixedThreadPool(5, threadFactory);
    this.plugin = plugin;

  }

  /**
   * Constructor for the ConcurrencyHandler class.
   * This constructor is used for testing purposes.
   */
  public ConcurrencyHandler() {
    this(null);
  }

  /**
   * This method runs a task.
   *
   * @param runnable The task to be run.
   * @param async A boolean indicating if the task should be run asynchronously.
   * @return a CompletableFuture that will be completed when the task is run.
   */
  public CompletableFuture<Void> run(Runnable runnable, boolean async) {

    if (async) {
      return CompletableFuture.runAsync(runnable, executorPool);
    } else {
      runnable.run();
      return CompletableFuture.completedFuture(null);
    }

  }

  /**
   * This method runs a task after multiple other tasks are completed.
   *
   * @param completableFutures The tasks to be completed before running the task.
   * @param runnable The task to be run.
   * @param async A boolean indicating if the task should be run asynchronously.
   * @return a CompletableFuture that will be completed when the task is run.
   */
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

  /**
   * This method runs a task after a delay.
   *
   * @param runnable The task to be run.
   * @param delay The delay before running the task.
   * @param timeUnit The unit of the delay.
   * @param async A boolean indicating if the task should be run asynchronously.
   * @return a CompletableFuture that will be completed when the task is run.
   */
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

  /**
   * This method supplies a result.
   *
   * @param supplier The supplier of the result.
   * @param async A boolean indicating if the result should be supplied asynchronously.
   * @return a CompletableFuture that will be completed with the result.
   */
  public <T> CompletableFuture<T> supply(Supplier<T> supplier, boolean async) {

    if (async) {
      return CompletableFuture.supplyAsync(supplier, executorPool);
    } else {
      return CompletableFuture.completedFuture(supplier.get());
    }

  }

  /**
   * This method accepts a result after a task is completed.
   *
   * @param completableFuture The task to be completed before accepting the result.
   * @param consumer The consumer of the result.
   * @param async A boolean indicating if the result should be accepted asynchronously.
   * @return a CompletableFuture that will be completed when the result is accepted.
   */
  public <T> CompletableFuture<Void> thenAccept(CompletableFuture<T> completableFuture,
      Consumer<? super T> consumer, boolean async) {

    if (async) {
      return completableFuture.thenAcceptAsync(consumer, executorPool);
    } else {
      consumer.accept(completableFuture.join());
      return CompletableFuture.completedFuture(null);
    }

  }

  /**
   * This method gets a list of futures.
   *
   * @param futures The futures to be included in the list.
   * @return an array of CompletableFutures.
   */
  public CompletableFuture<?>[] getListOfFutures(CompletableFuture<?>... futures) {
    return futures;
  }

  /**
   * This method runs a task on the main thread.
   *
   * @param runnable The task to be run.
   */
  public void runOnMainThread(Runnable runnable) {
    plugin.getServer().getScheduler().runTask(plugin, runnable);
  }

}