/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.dataModels.util;

import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.arrays.validator.ArraysValidator;
import org.sbml.jsbml.validator.SBMLValidator;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

public class Validate 
{
  private static final Message errorMessage = new Message();
  public static String getMessage()
  {
    return errorMessage.getMessage();
  }

  
  /**
   * Check consistency of sbml file.
   * 
   * @param file
   * @param doc
   * @param overdeterminedOnly
   * @return
   * @throws IOException
   * @throws SBMLException
   * @throws XMLStreamException
   * @throws BioSimException
   */
  public static boolean validateDoc(String file, SBMLDocument doc, boolean overdeterminedOnly) throws IOException, SBMLException, XMLStreamException, BioSimException
  {
    // TODO: added to turn off checking until libsbml bug is found
    //if (true) {
    //  return true;
    //}
    String message = "";
    long numErrors = 0;
    Preferences biosimrc = Preferences.userRoot();
    String validationMethod = biosimrc.get("biosim.general.validate", "");
    boolean warnings = biosimrc.get("biosim.general.warnings", "").equals("true");
    if (validationMethod.equals("libsbml") && Executables.libsbmlFound)
    {
      message += "Validation Problems Found by libsbml\n";
      org.sbml.libsbml.SBMLDocument document = null;
      // TODO: temporary hack because otherwise it hangs
      if (doc == null)
      {
        document = new org.sbml.libsbml.SBMLReader().readSBML(file);
      }
      else
      {
        document = new org.sbml.libsbml.SBMLReader().readSBMLFromString(new SBMLWriter().writeSBMLToString(doc));
      }
      if (document == null)
      {
        return false;
      }
      if (overdeterminedOnly)
      {
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_INTERNAL_CONSISTENCY, false);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
      }
      else
      {
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_INTERNAL_CONSISTENCY, true);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
      }
      if (warnings && !overdeterminedOnly)
      {
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_UNITS_CONSISTENCY, true);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MATHML_CONSISTENCY, true);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_SBO_CONSISTENCY, true);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MODELING_PRACTICE, true);
      }
      else
      {
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_UNITS_CONSISTENCY, false);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MATHML_CONSISTENCY, false);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_SBO_CONSISTENCY, false);
        document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_MODELING_PRACTICE, false);
      }

      long numberOfErrors = 0; //document.checkConsistency();
      if (!libsbml.LIBSBML_DOTTED_VERSION.equals("5.15.0")) {
        numberOfErrors = document.checkConsistency();
      } else {
        numberOfErrors = document.checkConsistency();
        System.out.println(libsbml.LIBSBML_DOTTED_VERSION);
        System.out.println("ERROR: please update libsbml.");
      }
      for (int i = 0; i < numberOfErrors; i++)
      {
        String error = document.getError(i).getMessage();
        if (error.startsWith("Due to the need to instantiate models"))
        {
          continue;
        }
        if (error.startsWith("The CompFlatteningConverter has encountered a required package"))
        {
          continue;
        }
        message += numErrors + ":" + error + "\n";
        numErrors++;
      }
      if (!overdeterminedOnly)
      {
        List<SBMLError> arraysErrors = ArraysValidator.validate(doc);
        for (int i = 0; i < arraysErrors.size(); i++)
        {
          String error = arraysErrors.get(i).getMessage();
          message += numErrors + ":" + error + "\n";
          numErrors++;
        }
      }
    }
    else if(validationMethod.equals("jsbml"))
    {
      message += "Validation Problems Found by JSBML\n";
      doc.checkConsistencyOffline();
      List<SBMLError> listOfError = doc.getErrorLog().getValidationErrors();
      for(SBMLError error : listOfError)
      {
        if(error.getMessage().length() > 0)
        {
          message += numErrors + ":" + error.getMessage() + "\n";
          numErrors++;
        }
      }
    }
    else
    {
      message += "Validation Problems Found by Webservice\n";
      SBMLDocument document = doc;
      if (document == null)
      {
        document = SBMLutilities.readSBML(file, null, null);
      }
      if (overdeterminedOnly)
      {
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, false);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, false);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
      }
      else
      {
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
      }
      if (warnings && !overdeterminedOnly)
      {
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, true);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, true);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, true);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, true);
      }
      else
      {
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, false);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, false);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, false);
        document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, false);
      }
      long numberOfErrors = document.checkConsistency(); 
      for (int i = 0; i < numberOfErrors; i++)
      {
        String error = document.getError(i).getMessage();
        if (error.startsWith("Due to the need to instantiate models"))
        {
          continue;
        }
        if (error.startsWith("The CompFlatteningConverter has encountered a required package"))
        {
          continue;
        }
        message += numErrors + ":" + error + "\n";
        numErrors++;
      }
      if (!overdeterminedOnly)
      {
        List<SBMLError> arraysErrors = ArraysValidator.validate(document);
        for (int i = 0; i < arraysErrors.size(); i++)
        {
          String error = arraysErrors.get(i).getMessage();
          message += numErrors + ":" + error + "\n";
          numErrors++;
        }
      }
    }

    if (numErrors > 0)
    {
      errorMessage.setLog(message);
      return true;
    }

    return false;
  }
}
