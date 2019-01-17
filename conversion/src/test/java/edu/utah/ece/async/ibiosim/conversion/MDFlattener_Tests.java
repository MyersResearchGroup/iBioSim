package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.w3c.jigsaw.pagecompile.GeneratedClassLoader;

import com.lowagie.text.Document;
/**
 * 
 * @author Pedro Fontanarrosa
 *
 */
public class MDFlattener_Tests {
	
	private static SBOLDocument doc;
	
	@BeforeClass
	public static void setupTest() throws SBOLValidationException, IOException, SBOLConversionException {

		String file_path = ConversionTestSuite.H_auto_regulatory_TU_file;
		doc = SBOLReader.read(new File(file_path));
		
	}

	@Test
	public void test_CD_size () throws SBOLValidationException, IOException, SBOLConversionException {
		
		Assert.assertEquals(7, doc.getComponentDefinitions().size());	
	}
}
