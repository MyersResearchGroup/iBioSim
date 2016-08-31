package learn.genenet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;

public class Run
{

	private static int	experiment;

	public static void run(String filename, String directory)
	{
		SpeciesCollection S = new SpeciesCollection();
		Experiments E = new Experiments();
		Encodings L = new Encodings();
		Thresholds T = new Thresholds();
		NetCon C = new NetCon();
		init(filename, S);
		loadExperiments(directory, S, E);
		Learn learn = new Learn(3);
		learn.learnNetwork(S, E, C, T, L);
		learn.getDotFile("method.gcm", directory, S, C);
		learn.getDotFile("method.dot", directory, S, C);
	}

	public static void loadExperiments(String directory, SpeciesCollection S, Experiments E)
	{
		File path = new File(directory);

		experiment = 0;

		for (File file : path.listFiles())
		{
			String name = file.getAbsolutePath();
			if (name.endsWith(".tsd"))
			{
				parse(name, S, E);
				experiment++;
			}
			else if (name.endsWith(".csv"))
			{
				parseCSV(name, S, E);
				experiment++;
			}
		}
	}

	public static void init(String filename, SpeciesCollection S)
	{
		try
		{
			SBMLDocument doc = SBMLReader.read(new File(filename));

			Model model = doc.getModel();

			for (Species species : model.getListOfSpecies())
			{
				S.addInterestingSpecies(species.getId());
			}
		}

		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void parseCSV(String filename, SpeciesCollection S, Experiments E)
	{
		Scanner scan = null;
		boolean isFirst = true;
		try
		{
			scan = new Scanner(new File(filename));
			int row = 0;
			while (scan.hasNextLine())
			{
				String line = scan.nextLine();

				String[] values = line.split(",");

				if (isFirst)
				{
					for (int i = 0; i < values.length; i++)
					{
						S.addSpecies(values[i], i);
					}
					isFirst = false;
				}
				else
				{
					for (int i = 0; i < values.length; i++)
					{
						E.addExperiment(experiment, row, i, Double.parseDouble(values[i]));
					}
					row++;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Could not find the file!");
		}
		finally
		{
			if (scan != null)
			{
				scan.close();
			}

		}
	}

	private static void parse(String filename, SpeciesCollection S, Experiments E)
	{
		InputStream input = null;

		try
		{

			String buffer = "";
			char state = 1;
			char read = 0;
			int row = 0;
			input = new FileInputStream(filename);

			int data = input.read();
			while (data != -1)
			{
				data = input.read();
				read = (char) data;

				if (read == ' ' || read == '\n')
				{
					continue;
				}

				switch (state)
				{
				case 1:
					if (read == '(')
					{
						state = 2;
						buffer = "";
						break;
					}
					else
					{
						return;
					}
				case 2:
					if (read == ')')
					{
						state = 3;

						row = 0;
						String[] ids = buffer.split(",");

						if (experiment == 0)
						{
							for (int i = 0; i < ids.length; i++)
							{
								S.addSpecies(ids[i].substring(1, ids[i].length() - 1), i);
							}
						}

						break;
					}
					else
					{
						buffer += read;

						break;
					}
				case 3:
					if (read == '(')
					{
						state = 4;
						buffer = "";
						break;
					}
					if (read == ',')
					{
						break;
					}
					if (read == ')')
					{
						return;
					}
					else
					{
						return;
					}
				default:
					if (read == ')')
					{
						state = 3;
						String[] values = buffer.replace(" ", "").split(",");

						for (int i = 0; i < values.length; i++)
						{
							E.addExperiment(experiment, row, i, Double.parseDouble(values[i]));
						}

						row++;
					}
					else
					{
						buffer += read;
					}
				}
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Could not find the file!");
		}
		catch (IOException e)
		{
			System.out.println("There was a problem when reading the file!");
		}
		finally
		{
			try
			{
				if (input != null)
				{
					input.close();
				}
			}
			catch (IOException e)
			{
				System.out.println("Failed to close input stream");
			}

		}
	}
}
