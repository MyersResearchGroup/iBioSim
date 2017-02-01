package dataModels.util;


public class Message {
  public static enum MessageType
  {
      PROGRESS,
      LOG,
      CONSOLE,
      CANCEL,
      NONE
  };
  
  private MessageType type;
  private String message;
  private int value;
  
  public Message()
  {
    this.type = MessageType.NONE;
  }
  
  public void setProgress(int value)
  {
    this.type = MessageType.PROGRESS;
    this.value = value;
    this.message = null;
  }
  
  public void setLog(String message)
  {
    this.type = MessageType.LOG;
    this.message = message;
  }
  
  public void setConsole(String message)
  {
    this.type = MessageType.CONSOLE;
    this.message = message;
  }
  
  public void setCancel()
  {
    this.type = MessageType.CANCEL;
    this.message = null;
  }
  
  public boolean isProgress()
  {
    return this.type == MessageType.PROGRESS;
  }
  
  public boolean isLog()
  {
    return this.type == MessageType.LOG;
  }
  
  public boolean isConsole()
  {
    return this.type == MessageType.CONSOLE;
  }
  
  public boolean isCancel()
  {
    return this.type == MessageType.CANCEL;
  }
  
  public String getMessage()
  {
    return message;
  }
  
  public int getValue()
  {
    return value;
  }
}
