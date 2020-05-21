package com.ricardojlrufino.eventbus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import com.ricardojlrufino.eventbus.dispatcher.DebounceEventDispatcher;
import com.ricardojlrufino.eventbus.dispatcher.SingleThreadEventDispatcher;

public class EventBus {
    
    private static EventBus instance;
    
    private Map<Object, Set<EventHandler>> holderList;
    private Map<Class<? extends EventMessage>, Set<EventHandler>> handlers;
    private Map<Class<? extends EventMessage>, EventDispatcher> dispacherMap;
    private List<EventBusListener> listeners;
    
    private EventDispatcher dispatcher;
    
    
    private EventBus() {
        handlers = new ConcurrentHashMap<>();
        holderList = new ConcurrentHashMap<>();
        dispacherMap = new ConcurrentHashMap<>();
        listeners = new CopyOnWriteArrayList<>();
        dispatcher = SingleThreadEventDispatcher.newInstance();
    }
    
    public synchronized static EventBus get() {
        if(instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    public void setDefaultDispatcher( EventDispatcher dispatcher ) {
        this.dispatcher = dispatcher;
    }
    
    /**
     * Register Global Handler. <br/>
     * Links an {@link EventMessage} to a specific {@link EventHandler}. <br/>
     * 
     * Be careful, as you must unregister when you no longer need to avoid memory leakes.<br/>
     * To make unregistration easier, you can use a holder, using {@link #register(Object, Class, EventHandler)}
     *
     * @param eventType The {@link EventMessage} to be registered
     * @param handler   The {@link EventHandler} that will be handling the event.
     */
    public synchronized static <E extends EventMessage> EventHandler<E> register(Class<E> eventType,EventHandler<E> handler) {
       
        Set<EventHandler> set = get().handlers.get(eventType);
        
        if(set == null) {
            set = newListImpl();
            get().handlers.put(eventType, set);
        }
        
        set.add(handler);

        return handler;
    }
    
    /**
     * Register a new handler, linked to the holder.
     * The holder is used to facilitate 'unregister', when the handler is no longer needed.
     * @param <E>
     * @param holder - Preferably the object that made the call to register the handler
     * @param eventType
     * @param handler
     * @return 
     */
    public synchronized static <E extends EventMessage> EventHandler<E> register(Object holder, Class<E> eventType,EventHandler<E> handler) {
        
        // Add to holders...
        Set<EventHandler> set = get().holderList.get(holder);
        if(set == null) {
            set = newListImpl();
            get().holderList.put(holder, set);
        }
        
        set.add(handler);
        
        return register(eventType, handler);
    }
    
    /**
     * Unregister all handlers, which are linked to the holder passed as a parameter
     * @param <E>
     * @param holder
     */
    public synchronized static <E extends EventMessage> void unregisterHandlers(Object holder) {
        Set<EventHandler> handlers = get().holderList.get(holder);
        if(handlers != null) {
            for (EventHandler eventHandler : handlers) {
                unregisterHandler(eventHandler);
            }
            get().holderList.remove(holder);
        }
    }
    
    
    /**
     * 
     * @param <E>
     * @param handler
     */
    public synchronized static <E extends EventMessage> void unregisterHandler(EventHandler<E> handler) {
        Set<Class<? extends EventMessage>> keySet = get().handlers.keySet();
        for (Class<? extends EventMessage> class1 : keySet) {
            get().handlers.get(class1).remove(handler);
        }
    }

    
    /**
     * Dispatches an {@link EventMessage} to registred handlers.
     *
     * @param event The {@link EventMessage} to be dispatched
     */
    @SuppressWarnings("unchecked")
    public static <E extends EventMessage> void notify(E event) {
        Set<EventHandler> handlers = get().handlers.get(event.getClass());
        
        EventBusListener busListener = get().listener;

        if (handlers != null) {
            // Check if as a custom dispatcher for this event.
            EventDispatcher eventDispatcher = (EventDispatcher) get().dispacherMap.get(event.getClass());

            // use default dispacher.
            if (eventDispatcher == null) eventDispatcher = get().dispatcher;
            
            
            for (EventHandler<E> handler : handlers) {
                busListener.beforeDispatch(event, handler, eventDispatcher);
                eventDispatcher.dispatch(event, handler, busListener);
            }
        }else {
            busListener.eventIgnored(event, get().dispatcher, "No Handler");
        }
    }

    /**
     * Allow use a custom {@link EventDispatcher} to handle this event type.
     * If you need your event to run on multiple threads or have some debounce technique, you can implement your own {@link EventDispatcher}.
     * @see DebounceEventDispatcher
     * @see SingleThreadEventDispatcher
     * @param eventType The {@link EventMessage} to be managed by the custom dispatcher
     * @param eventDispatcher 
     */
    public static <E extends EventMessage>  void configDispatcher( Class<E> eventType , EventDispatcher eventDispatcher ) {
        get().dispacherMap.put(eventType, eventDispatcher);
    }
    
    /**
     * Add a listener for the events that happen on the EventBus. It can be used to implement a logging system for example.
     * @param listener
     */
    public static void addBusListener(EventBusListener listener) {
        get().listeners.add(listener);
    }

    /**
     * Shutdown all running tasks and clear registed events and settings.
     * @param wait
     * @param millis
     */
    public static void shutdown(boolean wait, long millis) {
        
        get().dispatcher.shutdown(wait, millis);

        Collection<EventDispatcher> values = get().dispacherMap.values();
        for (EventDispatcher eventDispatcher : values) {
            eventDispatcher.shutdown(wait, millis);
        }
        
        get().handlers.clear();
        get().dispacherMap.clear();
        get().listeners.clear();
        instance = null;
    }
    
    /**
     * Returns the standard implementation, with concurrency control support.
     */
    private static <E> Set<E> newListImpl(){
        return new CopyOnWriteArraySet<E>();
    }
    
    
    private EventBusListener listener = new EventBusListener() {
        
        @Override
        public <E extends EventMessage> void beforeRun( E event , EventHandler<E> handler ) {
            synchronized (listeners) {
                for (EventBusListener eventBusListener : listeners) {
                    eventBusListener.beforeRun(event, handler);
                }
            }
        }
        
        @Override
        public <E extends EventMessage> void afterRun( E event , EventHandler<E> handler ) {
            synchronized (listeners) {
                for (EventBusListener eventBusListener : listeners) {
                    eventBusListener.afterRun(event, handler);
                }
            }
        }

        @Override
        public <E extends EventMessage> void beforeDispatch( E event , EventHandler<E> handler , EventDispatcher eventDispatcher ) {
            synchronized (listeners) {
                for (EventBusListener eventBusListener : listeners) {
                    eventBusListener.beforeDispatch(event, handler, eventDispatcher);
                }
            }
        }

        @Override
        public <E extends EventMessage> void eventIgnored( E event , EventDispatcher eventDispatcher , String reason ) {
            synchronized (listeners) {
                for (EventBusListener eventBusListener : listeners) {
                    eventBusListener.eventIgnored(event, eventDispatcher, reason);
                }
            }
        }
        
        public <E extends EventMessage> void onError(Exception e, E event, EventHandler<E> handler) {
            
            synchronized (listeners) {
                for (EventBusListener eventBusListener : listeners) {
                    eventBusListener.onError(e, event, handler);
                }
            }
        };
    };

    /**
     * Returns the registered handlers for the eventType
     * @param <E>
     * @param eventType
     * @return unmodifiableSet
     */
    public static  Set<EventHandler> getHandlers(Class<? extends EventMessage> eventType) {
        return Collections.unmodifiableSet((get().handlers.get(eventType)));
        
    }
  
}
