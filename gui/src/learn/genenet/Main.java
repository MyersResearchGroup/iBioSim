package learn.genenet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Main
{

	static int	experiment	= 0;

	public static void main(String[] args)
	{

		if (args.length < 2)
		{
			System.out.println("Not enough arguments");
			return;
		}
		SpeciesCollection S = new SpeciesCollection();
		Experiments E = new Experiments();
		Encodings L = new Encodings();
		Thresholds T = new Thresholds();
		NetCon C = new NetCon();

		init(args[0], S);

		for (int i = 1; i < args.length - 1; i++)
		{
			parse(args[i], S, E);
			experiment++;
		}

		Learn learn = new Learn(3);
		learn.learnNetwork(S, E, C, T, L);
		learn.getDotFile("test.dot", args[args.length - 1], S, C);
		C = new NetCon();
		learn.learnBaselineNetwork(S, E, C);
		learn.getDotFile("basic.dot", args[args.length - 1], S, C);
	}

	private static void init(String filename, SpeciesCollection S)
	{
		Scanner scanner = null;
		try
		{
			scanner = new Scanner(new File(filename));
			while (scanner.hasNext())
			{
				String id = scanner.next();
				S.addInterestingSpecies(id);
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found!");
		}
		finally
		{
			scanner.close();
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
