package br.com.luiza.inventory.utils;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.bukkit.Bukkit.getScheduler;


public final class EventScheduler<E extends Event> {

    private final Plugin plugin;
    private final Class<E> eventClazz;

    private long time;
    private TimeUnit timeUnit;
    private Runnable timeoutRunnable;
    private Predicate<E> predicate = Objects::nonNull;
    private EventThreadContext eventThreadContext;
    private Consumer<E> eventConsumer;
    private final ScheduledExecutorService executor;
    private RegisteredListener listener;

    public static <T extends Event> EventSchedulerBuilder<T> of(Plugin plugin, Class<T> clazz) {
        return new EventSchedulerBuilder<>(plugin, clazz);
    }

    private EventScheduler(Plugin plugin, Class<E> eventClazz) {
        this.plugin = plugin;
        this.eventClazz = eventClazz;

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public EventScheduler<E> schedule() {
        register(event -> {
            if (eventClazz.isInstance(event)) {
                E scheduledEvent = (E) event;

                if (predicate.test(scheduledEvent)) {
                    if (eventThreadContext == EventThreadContext.SYNC) {
                        getScheduler().runTask(plugin, () -> this.eventConsumer.accept(scheduledEvent));
                    } else if (eventThreadContext == EventThreadContext.ASYNC) {
                        executor.execute(() -> this.eventConsumer.accept(scheduledEvent));
                    } else {
                        throw new RuntimeException("Thread Context");
                    }

                    this.unregister();
                    this.executor.shutdownNow();
                }
            }
        });

        executor.schedule(() -> {
            unregister();
            timeoutRunnable.run();
        }, time, timeUnit);

        return this;
    }

    private void register(Consumer<Event> onEvent) {
        this.listener = new RegisteredListener(
          new Listener() {
          },
          (listener, event) -> onEvent.accept(event),
          EventPriority.NORMAL,
          plugin,
          false
        );
        for (HandlerList handler : HandlerList.getHandlerLists()) {
            handler.register(listener);
        }
    }

    private void unregister() {
        for (HandlerList handler : HandlerList.getHandlerLists()) {
            handler.unregister(listener);
        }
    }

    public static class EventSchedulerBuilder<E extends Event> {

        private final EventScheduler<E> awaiter;

        public EventSchedulerBuilder(Plugin plugin, Class<E> clazz) {
            this.awaiter = new EventScheduler<>(plugin, clazz);
        }

        public EventSchedulerBuilder<E> filter(Predicate<E> predicate) {
            Objects.requireNonNull(predicate, "Predicate cannot be null.");

            awaiter.predicate = predicate;
            return this;
        }

        public EventSchedulerBuilder<E> thenExecuteSync(Consumer<E> consumer) {
            Objects.requireNonNull(consumer, "Consumer cannot be null.");

            awaiter.eventThreadContext = EventThreadContext.SYNC;
            awaiter.eventConsumer = consumer;
            return this;
        }

        public EventSchedulerBuilder<E> thenExecuteAsync(Consumer<E> consumer) {
            Objects.requireNonNull(consumer, "Consumer cannot be null.");

            awaiter.eventThreadContext = EventThreadContext.ASYNC;
            awaiter.eventConsumer = consumer;
            return this;
        }

        public EventSchedulerBuilder<E> orTimeOutAfter(long timeout, TimeUnit unit, Runnable runnable) {
            awaiter.time = timeout;
            awaiter.timeUnit = unit;
            awaiter.timeoutRunnable = runnable;

            return this;
        }

        public EventScheduler<E> schedule() {
            return awaiter.schedule();
        }
    }

    private enum EventThreadContext {
        SYNC,
        ASYNC
    }
}