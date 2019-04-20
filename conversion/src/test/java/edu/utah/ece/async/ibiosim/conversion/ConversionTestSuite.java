package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	MDFlattener_Tests.class,
	CelloModeling_Tests.class
})

/**
 * 
 * @author Pedro Fontanarrosa
 *
 */
public class ConversionTestSuite {
	
	
	protected static String MDFlattener_Dir = "src" + File.separator + "test" + File.separator + "resources" + File.separator + 
			"edu"+ File.separator + "utah" + File.separator + "ece" + File.separator + "async" + File.separator + "ibiosim" + File.separator + "conversion" +
			File.separator +"MDFlattener_SBOL_Files";
	protected static String H_auto_regulatory_TU_file = MDFlattener_Dir + File.separator + "H_AutoRegulatory.xml";
}
