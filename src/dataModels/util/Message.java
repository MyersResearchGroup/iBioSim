package dataModels.util;


public class Message {
  public static enum MessageType
  {
      ERROR,
      DIALOG,
      PROGRESS,
      LOG,
      CONSOLE,
      CANCEL,
      NONE
  };
  
  private MessageType type;
  private String message;
  private String title;
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
  
  public void setErrorDialog(String title, String message)
  {
    this.type = MessageType.ERROR;
    this.message = message;
    this.title = title;
  }
  
  public void setDialog(String title, String message)
  {
    this.type = MessageType.DIALOG;
    this.message = message;
    this.title = title;
  }
  
  
  public void setLog(String message)
  {
    this.type = MessageType.LOG;
    this.message = message;
    this.title = null;
  }
  
  public void setConsole(String message)
  {
    this.type = MessageType.CONSOLE;
    this.message = message;
    this.title = null;
  }
  
  public void setCancel()
  {
    this.type = MessageType.CANCEL;
    this.message = null;
    this.title = null;
  }
  
  public boolean isDialog()
  {
    return this.type == MessageType.DIALOG;
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
  
  public boolean isErrorDialog()
  {
    return this.type == MessageType.ERROR;
  }
  
  
  public String getMessage()
  {
    return message;
  }
  
  public int getValue()
  {
    return value;
  }
  
  public String getTitle()
  {
    return title;
  }
}
