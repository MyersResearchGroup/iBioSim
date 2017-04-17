/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.dataModels.util;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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
