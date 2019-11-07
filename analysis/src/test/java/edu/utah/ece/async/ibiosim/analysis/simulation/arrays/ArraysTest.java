package edu.utah.ece.async.ibiosim.analysis.simulation.arrays;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalSSADirectSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

public class ArraysTest {

  private AnalysisProperties properties;

  @Before
  public void setUp() throws Exception {
    String root = ArraysTest.class.getResource(".").getPath();
    properties = new AnalysisProperties("", "", root, false);
  }

  @Test
  public void test_basic_setup() {
    try {
      properties.setModelFile("test_1.xml");
      HierarchicalSimulation simulator = new HierarchicalSSADirectSimulator(properties);
//      ModelSetup.setupModels(simulator, ModelType.NONE);

    }
    catch (IOException | XMLStreamException | BioSimException e) {
      fail("Could not initialize");
    }
  }
}
