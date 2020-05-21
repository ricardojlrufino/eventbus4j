package benchmark.eventbus;

import org.openjdk.jmh.infra.Blackhole;

import com.ricardojlrufino.eventbus.EventMessage;

public interface UIEvents {
  
  public static Class<EventBenchmark> BOARD_CHANGE = EventBenchmark.class;
  public static Class<EventUserChange> USER_CHANGE = EventUserChange.class;
  
  public static class EventBenchmark implements EventMessage {

    private Blackhole board;
    
    public EventBenchmark(Blackhole blackhole) {
      super();
      this.board = blackhole;
    }

    public Blackhole getBlackhole() {
      return board;
    }
    

    @Override
    public String toString() {
      return "Event[Bord:"+board+"]";
    }
    
  }
  
  public static class EventUserChange implements EventMessage {

    private String name;
    
    public EventUserChange(String board) {
      super();
      this.name = board;
    }

    public String getUser() {
      return name;
    }
  
    @Override
    public String toString() {
      return "User[name:"+name+"]";
    }
    
  }
}