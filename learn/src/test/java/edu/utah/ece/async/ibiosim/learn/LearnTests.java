package edu.utah.ece.async.ibiosim.learn;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;


public class LearnTests {

  private String root = "src" + File.separator + "test" + File.separator + 
      "resources" + File.separator;
  @Test
  public void test01() {
    String directory = root + File.separator + "simpleDesign" + File.separator;
    String filename = directory + "simpleDesign.xml";
    Learn.main(new String[]{filename, directory});
    //TODO: add check
  }
  
  @Test
  public void test02() {
    String directory = root + File.separator + "degradation" + File.separator;
    String filename = directory + "degradation.xml";
    Learn.main(new String[]{"-e", filename, directory});
    //TODO: add check
  }
}
