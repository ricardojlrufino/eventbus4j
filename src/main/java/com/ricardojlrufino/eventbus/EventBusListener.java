package com.ricardojlrufino.eventbus;

/**
 * Listerer for events of bus.
 * Use to implment time mensurements or logging. 
 */
public abstract class EventBusListener {
    
    public <E extends EventMessage> void beforeRun(E event, EventHandler<E> handler) {};
    
    public <E extends EventMessage> void afterRun(E event, EventHandler<E> handler) {};

    /**
     * Called before the event is "scheduled" to run. It is the responsibility of the dispatcher to accept or not the event.
     * This method is executed on the same thread as the method that triggered the event, thus allowing tracking.
     * @param <E>
     * @param event
     * @param handler
     * @param eventDispatcher
     */
    public <E extends EventMessage> void beforeDispatch( E event , EventHandler<E> handler , EventDispatcher eventDispatcher ) {};
    
    /**
     * Event raised when the dispatcher ignores the event for some reason.
     * This method is executed on the same thread as the method that triggered the event, thus allowing tracking.
     * 
     * Use: {@link EventBusUtils#printStack()}
     * 
     * @param <E>
     * @param event
     * @param eventDispatcher
     * @param reason
     */
    public <E extends EventMessage> void eventIgnored( E event, EventDispatcher eventDispatcher, String reason ) {};

    
    /**
     * Listener for exceptions in handler.
     * @param <E>
     * @param e
     * @param event
     * @param handler
     */
    public <E extends EventMessage> void onError( Exception e , E event , EventHandler<E> handler ) {};

}
