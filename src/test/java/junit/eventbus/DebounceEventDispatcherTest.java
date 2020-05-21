package junit.eventbus;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.ricardojlrufino.eventbus.EventBus;
import com.ricardojlrufino.eventbus.dispatcher.DebounceEventDispatcher;

import junit.eventbus.UIEvents.EventBoardChange;

public class DebounceEventDispatcherTest {

  @Before
  public void setup() {
    EventBus.configDispatcher(UIEvents.BOARD_CHANGE, new DebounceEventDispatcher(1000));
  }
  
  
  /**
   * Here the event will only be run once, because several events happen "indiscriminately", 
   * and we don't want that.
   */
  @Test
  public void testCallsFastMethodNoInterval() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("BoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        atomicInteger.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    
    // Simulate some event calls....
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        EventBus.notify(new EventBoardChange("board 1"));
        EventBus.notify(new EventBoardChange("board 2"));
        EventBus.notify(new EventBoardChange("board 3"));
        EventBus.notify(new EventBoardChange("board 4"));
        EventBus.notify(new EventBoardChange("board 5"));
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(1, atomicInteger.get());

  }
  
  @Test
  public void testCallsFastMethodWith200Interval() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("BoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        atomicInteger.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    
    // Simulate some event calls....
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EventBus.notify(new EventBoardChange("board 1"));
          Thread.sleep(200);
          EventBus.notify(new EventBoardChange("board 2"));
          Thread.sleep(200);
          EventBus.notify(new EventBoardChange("board 3"));
          Thread.sleep(200);
          EventBus.notify(new EventBoardChange("board 4"));
          Thread.sleep(200);
          EventBus.notify(new EventBoardChange("board 5"));

        
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(1, atomicInteger.get());
  }
  
  /**
   * The case intends to evaluate the main rule, which is to allow a minimum execution interval, 
   * to avoid performance problems.
   */
  @Test
  public void testCallsFastMethodWith300Interval() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("BoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        atomicInteger.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    
    // Simulate some event calls....
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EventBus.notify(new EventBoardChange("board 1"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 2"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 3"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 4"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 5"));

        
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }


    EventBus.shutdown(true, 1000 * 30);

    assertEquals(2, atomicInteger.get());
  }
  
  
  /**
   * This case intends to test a situation where an event is called several times, but the handler is slow ... (more than the debounce).
   * In this situation the following events will be ignored, as the method is still running ...
   */
  @Test
  public void testCallsSlowMethodWith300Interval() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("BoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(1500);
        atomicInteger.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    
    // Simulate some event calls....
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          EventBus.notify(new EventBoardChange("board 1"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 2"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 3"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 4"));
          Thread.sleep(300);
          EventBus.notify(new EventBoardChange("board 5"));

        
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(20, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(1, atomicInteger.get());

  }

  public interface UIEvents {
    public static Class<EventBoardChange> BOARD_CHANGE = EventBoardChange.class;
  }

}
