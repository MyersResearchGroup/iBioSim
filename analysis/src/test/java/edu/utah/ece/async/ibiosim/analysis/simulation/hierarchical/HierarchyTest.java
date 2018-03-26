package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalSSADirectSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;


public class HierarchyTest {
  private AnalysisProperties properties;
  @Before
  public void setUp() throws Exception {
    String root = HierarchyTest.class.getResource(".").getPath();
    properties = new AnalysisProperties("", "", root, false);
  }


  @Test
  public void test_basic_setup() {
    try {
      properties.setModelFile("00001-sbml-l3v2.xml");
      HierarchicalSimulation simulator = new HierarchicalSSADirectSimulator(properties);
      ModelSetup.setupModels(simulator, ModelType.NONE);
      assertEquals(simulator.getListOfHierarchicalModels().size(), 1);
      
      HierarchicalModel model = simulator.getListOfHierarchicalModels().get(0);
      assertEquals(model.getReactions().size(), 1);
      assertEquals(model.getNode("S1").getState().getState(0).getStateValue(), 0.00015, 1e-9);
      assertEquals(model.getNode("S2").getState().getState(0).getStateValue(), 0, 0);
      assertEquals(model.getNode("compartment").getState().getState(0).getStateValue(), 1, 0);
      assertEquals(model.getNode("k1").getState().getState(0).getStateValue(), 1, 0);
    } catch (IOException | XMLStreamException | BioSimException e) {
      fail("Could not initialize");
    }
  }
}
