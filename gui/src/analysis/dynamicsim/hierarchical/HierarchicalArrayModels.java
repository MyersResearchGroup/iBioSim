package analysis.dynamicsim.hierarchical;

import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.Submodel;

public abstract class HierarchicalArrayModels extends HierarchicalSimulationFunctions
{

	public HierarchicalArrayModels(String SBMLFileName, String rootDirectory,
			String outputDirectory, double timeLimit, double maxTimeStep, double minTimeStep,
			JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction)
			throws IOException, XMLStreamException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, timeLimit, maxTimeStep, minTimeStep,
				progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType,
				abstraction);

	}

	public void setupArrayedModels()
	{
		ArraysSBasePlugin arrays;

		Model model = getDocument().getModel();

		CompModelPlugin comp = (CompModelPlugin) model.getPlugin("comp");

		for (Submodel sub : comp.getListOfSubmodels())
		{

			arrays = (ArraysSBasePlugin) sub.getExtension("arrays");

			if (arrays != null)
			{
				addValue(arrays, model, sub.getId());

				ModelState arrayedState = getSubmodels().remove(sub.getId());

				getArrayModels().put(sub.getId(), arrayedState);
			}
		}
	}

	private void addValue(ArraysSBasePlugin arrays, Model model, String id)
	{
		ModelState state = getSubmodels().get(id);
		int[] sizes = new int[arrays.getDimensionCount()];
		for (Dimension dim : arrays.getListOfDimensions())
		{
			if (model.getParameter(dim.getSize()) == null)
			{
				return;
			}
			sizes[dim.getArrayDimension()] = (int) model.getParameter(dim.getSize()).getValue() - 1;
		}

		setupArrayValue(state, id, sizes, new int[sizes.length]);
	}

	private void setupArrayValue(ModelState state, String id, int[] sizes, int[] indices)
	{
		String newId = id;

		for (int i = indices.length - 1; i >= 0; i--)
		{
			newId = newId + "_" + indices[i];
		}
		ModelState newState = state.clone();
		newState.setID(newId);
		getSubmodels().put(newId, newState);

		if (Arrays.equals(sizes, indices))
		{
			return;
		}

		indices[0]++;
		for (int i = 0; i < indices.length - 1; i++)
		{
			if (indices[i] > sizes[i])
			{
				indices[i] = 0;
				indices[i + 1]++;
			}
		}
		setupArrayValue(state, id, sizes, indices);
	}
}
