package edu.utah.ece.async.frontend;

import java.net.URL;

import javax.swing.ImageIcon;

public class ResourceManager {
  
  public static URL getResource(String fileName)
  {
    return ResourceManager.class.getResource("../" + fileName);
  }
  
  public static ImageIcon getImageIcon(String fileName)
  {
    return new ImageIcon(ResourceManager.class.getResource("../icons/" + fileName));
  }
  
}
