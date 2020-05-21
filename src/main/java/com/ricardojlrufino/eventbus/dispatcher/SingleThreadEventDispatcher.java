package com.ricardojlrufino.eventbus.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ricardojlrufino.eventbus.EventBusListener;
import com.ricardojlrufino.eventbus.EventDispatcher;
import com.ricardojlrufino.eventbus.EventHandler;
import com.ricardojlrufino.eventbus.EventMessage;

public class SingleThreadEventDispatcher implements EventDispatcher {

    private ExecutorService executor;
    
    private SingleThreadEventDispatcher() {
        executor = Executors.newSingleThreadExecutor(new ThreadFactoryImpl("SingleThreadEventDispatcher"));
    }
    
    /**
     * Clients should not call this method directly
     */
    public static SingleThreadEventDispatcher newInstance() {
        return new SingleThreadEventDispatcher();
    }
    
    public SingleThreadEventDispatcher(Class <?> event) {
        executor = Executors.newSingleThreadExecutor(new ThreadFactoryImpl(event.getSimpleName()));
    }

    @Override
    public <E extends EventMessage> void dispatch( E event , EventHandler<E> handler , EventBusListener busListener ) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                busListener.beforeRun(event, handler);
                
                try {
                    handler.onEvent(event);
                } catch (Exception e) {
                    busListener.onError(e, event, handler);
                }
                
                busListener.afterRun(event, handler);
            }
        });
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

    static class ThreadFactoryImpl implements ThreadFactory {
        
        private static Map<String, AtomicInteger> count = new HashMap<>();

        private String name;

        public ThreadFactoryImpl(String name) {
            super();
            AtomicInteger integer = count.get(name);
            if(integer == null) integer = new AtomicInteger(0);
            int index = integer.incrementAndGet();
            this.name = name + "-" + index;
        }

        @Override
        public Thread newThread( Runnable r ) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(name);
            return thread;
        }
    }
}
