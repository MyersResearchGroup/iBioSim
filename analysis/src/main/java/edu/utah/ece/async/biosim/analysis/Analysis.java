package edu.utah.ece.async.biosim.analysis;

/**
 * Command line method for running the analysis jar file.  
 * <p>
 * Requirements:
 * <p>
 * inputfile
 * <p>
 * 
 * Options:
 * <p>
 *
 *
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Analysis {

	private static void usage() {
	  //TODO:
		System.err.println("Description:\n");
		System.err.println("Usage:\n");
		System.err.println("Required:\n");
		System.err.println("Options:\n");
		System.exit(1);
	}

	public static void main(String[] args) {
	
	  if(args.length  < 2)
	  {
	    usage();
	  }
	  
	  for(String flag : args)
	  {
	    if(flag.length() == 2 && flag.charAt(0) == '-')
	    {
	      switch(flag.charAt(1))
	      {
	        case 'h':
	          usage();
	          break;
	        default:
	          usage();
	          break;
	      }
	    }
	    else if(flag.endsWith(".xml"))
	    {
	      
	    }
	    else
	    {
	      usage();
	    }
	  }
	} 
}
