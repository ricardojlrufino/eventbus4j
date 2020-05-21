package com.ricardojlrufino.eventbus;

public final class EventBusUtils {
    
    public static <E extends EventMessage> void printStack( E event , EventHandler<E> handler ) {
        Throwable throwable = new Throwable();
        
        StackTraceElement ele = throwable.getStackTrace()[4];
        
        System.err.println("[Event: "+event.getClass().getSimpleName() +"] on: " + ele);
        System.err.println("`--[Handler: "+handler+"] [THREAD: "+Thread.currentThread().getName() +"]");
        
        throwable.printStackTrace();
        
    }

}
