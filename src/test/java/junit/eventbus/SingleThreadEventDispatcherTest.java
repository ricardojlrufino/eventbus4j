package junit.eventbus;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.ricardojlrufino.eventbus.EventBus;
import com.ricardojlrufino.eventbus.EventBusListener;
import com.ricardojlrufino.eventbus.EventDispatcher;
import com.ricardojlrufino.eventbus.EventHandler;
import com.ricardojlrufino.eventbus.EventMessage;
import com.ricardojlrufino.eventbus.dispatcher.SingleThreadEventDispatcher;

import junit.eventbus.UIEvents.EventBoardChange;
import junit.eventbus.UIEvents.EventUserChange;

public class SingleThreadEventDispatcherTest {

  @Before
  public void setup() {
  }
  
  @Test
  public void testMutipleEventsSameHandler() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EventBoardChanged: " + event.getBoard() + "| thread:"
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
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(5, atomicInteger.get());

  }
 
  
  @Test
  public void testMultipleHandlersSameEvent() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EVENT1: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        atomicInteger.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    
    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EVENT2: " + event.getBoard() + "| thread:"
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
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(2, atomicInteger.get());

  }
  
  
  @Test
  public void testAllSlowEventsExecuted() {

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EventBoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(500);
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
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(5, atomicInteger.get());

  }
  
  
  @Test
  public void testDiferentEventToDiferenteHandler() {

    AtomicInteger userCount = new AtomicInteger();
    AtomicInteger boardCount = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EventBoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        boardCount.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    
    EventBus.register(UIEvents.USER_CHANGE, event -> {
      System.out.println("User Change: " + event.getUser() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        userCount.incrementAndGet();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    
    // Simulate some event calls....
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        EventBus.notify(new EventUserChange("user 1"));
        EventBus.notify(new EventBoardChange("board 1"));
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(1, userCount.get());
    assertEquals(1, boardCount.get());

  }

  /**
   * This allows you to create different threads for handling events.
   * Useful when you have heavy events and want to deal with a different thread
   */
  @Test
  public void testSpecificDisptachesAndThreadNames() {
    
    EventBus.configDispatcher(UIEvents.USER_CHANGE, new SingleThreadEventDispatcher(UIEvents.USER_CHANGE));
    EventBus.configDispatcher(UIEvents.BOARD_CHANGE, new SingleThreadEventDispatcher(UIEvents.BOARD_CHANGE));
    
    Set<String> threadsNames = new HashSet();

    AtomicInteger atomicInteger = new AtomicInteger();

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EventBoardChanged: " + event.getBoard() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        atomicInteger.incrementAndGet();
        synchronized (threadsNames) {
          threadsNames.add(Thread.currentThread().getName());
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    
    EventBus.register(UIEvents.USER_CHANGE, event -> {
      System.out.println("User Change: " + event.getUser() + "| thread:"
                         + Thread.currentThread().getName());
      try {
        Thread.sleep(10);
        atomicInteger.incrementAndGet();
        synchronized (threadsNames) {
          threadsNames.add(Thread.currentThread().getName());
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    
    // Simulate some event calls....
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new Runnable() {
      @Override
      public void run() {
        EventBus.notify(new UIEvents.EventBoardChange("board 1"));
        EventBus.notify(new UIEvents.EventUserChange("user 2"));
      }
    });
    
    // Wait simulation finish
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(2, atomicInteger.get());
    assertEquals(2, threadsNames.size());

  }
  
  @Test
  public void testBusListeners() {
    
    AtomicInteger atomicInteger = new AtomicInteger();
    
    EventBus.addBusListener(new EventBusListener() {
      
      @Override
      public <E extends EventMessage> void beforeRun(E event,
                                                     EventHandler<E> handler) {
        System.out.println(">>>> beforeRun " + event);
        atomicInteger.incrementAndGet();
      }
      
      @Override
      public <E extends EventMessage> void afterRun(E event,
                                                    EventHandler<E> handler) {
        System.out.println(">>>> afterRun " + event);
        atomicInteger.incrementAndGet();
      }

    @Override
    public <E extends EventMessage> void beforeDispatch( E event , EventHandler<E> handler , EventDispatcher eventDispatcher ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <E extends EventMessage> void eventIgnored( E event , EventDispatcher eventDispatcher , String reason ) {
        // TODO Auto-generated method stub
        
    }
    });
    

    EventBus.register(UIEvents.BOARD_CHANGE, event -> {
      System.out.println("EventBoardChanged: " + event.getBoard() + "| thread:"
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
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
    }

    EventBus.shutdown(true, 1000 * 30);

    assertEquals(15, atomicInteger.get());
  }
  
}
