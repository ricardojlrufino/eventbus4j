package com.ricardojlrufino.eventbus.dispatcher;

import java.util.concurrent.TimeUnit;

public class DebounceRunnable implements Runnable {

    private long delay;
    
    private Runnable runnable;
    
    public DebounceRunnable(Runnable task, long delayMillis) {
        this.runnable = task;
        this.delay = delayMillis;
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        runnable.run();
        long durationInNano = (System.nanoTime() - start);  //Total execution time in nano seconds
        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNano);  //Total execution time in nano seconds
        
        // No need delay
        if(durationInMillis >= delay) return;
        
        try {
            Thread.sleep(delay - durationInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
