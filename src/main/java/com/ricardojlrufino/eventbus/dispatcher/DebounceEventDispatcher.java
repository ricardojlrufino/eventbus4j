package com.ricardojlrufino.eventbus.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.ricardojlrufino.eventbus.EventBusListener;
import com.ricardojlrufino.eventbus.EventDispatcher;
import com.ricardojlrufino.eventbus.EventHandler;
import com.ricardojlrufino.eventbus.EventMessage;
import com.ricardojlrufino.eventbus.dispatcher.SingleThreadEventDispatcher.ThreadFactoryImpl;

/**
 * A Debouncer is responsible for executing a task with a delay, and cancelling
 * any previous unexecuted task before doing so.
 */
public class DebounceEventDispatcher implements EventDispatcher {

    private ExecutorService executor;
    private Future<?> future;
    private long delay;
    
    public static String MESSAGE_TO_MANY_CALLS = "TO_MANY_CALLS";

    public DebounceEventDispatcher(long delay) {
        this.delay = delay;
        this.executor = Executors.newSingleThreadExecutor(new SingleThreadEventDispatcher.ThreadFactoryImpl("DebounceEventDispatcher"));
    }

    @Override
    public <E extends EventMessage> void dispatch( E event , EventHandler<E> handler , EventBusListener listener) {
        if (future != null && !future.isDone()) {
            listener.eventIgnored(event, this, MESSAGE_TO_MANY_CALLS);
            return;
        }
        
        future = executor.submit(new DebounceRunnable(() -> {
            listener.beforeRun(event, handler);
            try {
                handler.onEvent(event);
            } catch (Exception e) {
                listener.onError(e, event, handler);
            }
            listener.afterRun(event, handler);
        }  , delay));
    }

    @Override
    public void shutdown( boolean wait , long millis ) {
        executor.shutdown();

        if (wait) {
            try {
                if (!executor.awaitTermination(millis, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }
}