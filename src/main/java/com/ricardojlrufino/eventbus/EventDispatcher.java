package com.ricardojlrufino.eventbus;

/**
 * This class is responsible to dispatch events.
 * The standard implementation runs the events on the same thread where the event was generated.
 */
public interface EventDispatcher {
    
    /**
     * Dispatches an {@link Event} depending on it's type.
     *
     * @param event The {@link Event} to be dispatched
     */
    public <E extends EventMessage> void dispatch(E event, EventHandler<E> handler, EventBusListener busListener );
    
    
    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     */
    void shutdown( boolean wait , long millis);



}
