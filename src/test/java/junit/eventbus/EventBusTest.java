package junit.eventbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ConcurrentModificationException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.ricardojlrufino.eventbus.EventBus;
import com.ricardojlrufino.eventbus.EventBusListener;
import com.ricardojlrufino.eventbus.EventHandler;
import com.ricardojlrufino.eventbus.EventMessage;

import junit.eventbus.UIEvents.EventBoardChange;

public class EventBusTest {
    
    @Test
    public void testRegisterAndUnregister() {
      EventHandler<EventBoardChange> handler = EventBus.register(junit.eventbus.UIEvents.BOARD_CHANGE, event -> {
          System.out.println("nothong");
      });
      
      
      Set<EventHandler<EventBoardChange>> handlers = EventBus.getHandlers(UIEvents.BOARD_CHANGE);
      
      assertEquals(1, handlers.size());
      
      EventBus.unregisterHandler(handler);
      
      handlers = EventBus.getHandlers(UIEvents.BOARD_CHANGE);
      assertEquals(0, handlers.size());
      
    }
    
    @Test
    public void testRegisterAndUnregisterHolder() {

        Object holder = new Object();

        EventBus.register(holder, junit.eventbus.UIEvents.BOARD_CHANGE, event -> {
            System.out.println("nothong");
        });

        Set<EventHandler<EventBoardChange>> handlers = EventBus.getHandlers(UIEvents.BOARD_CHANGE);

        assertEquals(1, handlers.size());

        EventBus.unregisterHandlers(holder);

        handlers = EventBus.getHandlers(UIEvents.BOARD_CHANGE);
        assertEquals(0, handlers.size());

    }
    
    
    @Test
    public void testUnregisterNoUsedHolder() {

        EventBus.register(UIEvents.BOARD_CHANGE, event -> {
            System.out.println("nothong");
        });

        Set<EventHandler<EventBoardChange>> handlers = EventBus.getHandlers(UIEvents.BOARD_CHANGE);

        assertEquals(1, handlers.size());

        // NO USED !
        EventBus.unregisterHandlers(new Object());

        handlers = EventBus.getHandlers(UIEvents.BOARD_CHANGE);
        assertEquals(1, handlers.size());

    }
    
    @Test
    public void testHanderException() {

        AtomicBoolean erro = new AtomicBoolean();
        
        EventBus.addBusListener(new EventBusListener() {
            @Override
            public <E extends EventMessage> void onError( Exception e , E event , EventHandler<E> handler ) {
                erro.set(true);
            }
        });

        EventBus.register(junit.eventbus.UIEvents.BOARD_CHANGE, event -> {
            throw new RuntimeException("teste");
        });

        EventBus.notify(new EventBoardChange("board:"));
        
        EventBus.shutdown(true, 1000);

        assertTrue(erro.get());

    }
    
    @Test
    public void testNotifyConcurrent() throws InterruptedException, ExecutionException {
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        // Simulate register
        executor.execute(() -> {
            int counter = 0;
            while(true) {
                final int c = ++counter;
                try {
//                    System.out.println("Resgister handler: " + c);
                    EventBus.register(UIEvents.BOARD_CHANGE, new EventHandler<EventBoardChange>() {
                        @Override
                        public void onEvent( EventBoardChange event ) {
//                            System.out.println("onEVENT: " + event + ", ON HANDLER: " + c);
                        }
                    });
                    
                } catch (ConcurrentModificationException e1) {
                    e1.printStackTrace();
                    fail("ConcurrentModificationException");
                }
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e2) {}
            }
        });
        
        // Simulate some event calls....
        Future<?> future = executor.submit(() -> {
              int counter = 0;
              int max = 500;
              while(true) {
                  final int c = ++counter;
                  if(max == c) break;
                  
                  try {
                      System.out.println("NOTIFY:" +c + "/" + max);
                      EventBus.notify(new EventBoardChange("board:" +c));
                  } catch (ConcurrentModificationException e1) {
                      e1.printStackTrace();
                      fail("ConcurrentModificationException");
                  }
                  
                  try {
                      Thread.sleep(5);
                  } catch (InterruptedException e2) {}
              }
          });
        
        future.get(); 
        
        EventBus.shutdown(true, 1000);
    }
}
