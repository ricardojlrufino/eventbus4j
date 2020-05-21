package junit.eventbus;

import com.ricardojlrufino.eventbus.EventMessage;

public interface UIEvents {
  
  public static Class<EventBoardChange> BOARD_CHANGE = EventBoardChange.class;
  public static Class<EventUserChange> USER_CHANGE = EventUserChange.class;
  
  public static class EventBoardChange implements EventMessage {

    private String board;
    
    public EventBoardChange(String board) {
      super();
      this.board = board;
    }

    public String getBoard() {
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