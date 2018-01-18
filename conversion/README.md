The conversion package currently supports three types of data conversion and VPR API model generation. 

1. Design to modeling: [SBOL2SBML](http://pubs.acs.org/doi/10.1021/sb5003289)  
2. Modeling to Design: [SBML2SBOL](http://pubs.acs.org/doi/abs/10.1021/acssynbio.5b00212)  
3. Interconversion supported by the libSBOLj2 library: [SBOL1 to SBOL2](http://ieeexplore.ieee.org/document/7440806/)

SBOL2SBML and SBML2SBOL can be build as a standalone JAR. 
There is also a main Java [Converter.java](https://github.com/MyersResearchGroup/iBioSim/blob/master/conversion/src/main/java/edu/utah/ece/async/ibiosim/conversion/Converter.java) file that supports all three types of conversion.

## How to use it:

After building the executable jar, you need to invoke the following command:

```
  java -jar target/iBioSim-conversion-3.0.0-SNAPSHOT-jar-with-dependencies.jar [options] input
```

where 

| Required        |  Description  |
| -------------   | ------------- |
| inputFile     | full path to input file |

| Options        |  Description  |
| -------------   | ------------- |
| -b  | check best practices. | 
| -cf  | The name of the file that will be produced to hold the result of the second SBOL file, if SBOL file diff was selected. | 
| -d  | display detailed error trace | 
| -e | The second SBOL file to compare to the main SBOL file. |
| -esf |  Export SBML hierarchical models in a single output file. |
| -f |  continue after first error. |
| -i  | allow SBOL document to be incomplete. |
| -l  <language> | specifies language (SBOL1/SBOL2/GenBank/FASTA/SBML) for output (default=SBOL2). To output FASTA or GenBank, no SBOL default URI prefix is needed. |
| -mf | The name of the file that will be produced to hold the result of the main SBOL file, if SBOL file diff was selected. |
| -o  <outputFile>| specifies the full path of the output file produced from the converter |
| -no | indicate no output file to be generated from validation. Instead, print result to console/command line. |
| -oDir [value] | output directory when SBOL to SBML conversion is performed and multiple SBML files are produced for individual submodels |
| -p [value] | default URI prefix to set an SBOLDocument |
| -rsbml [value] | The full path of external SBML files to be referenced in the SBML2SBOL conversion |
| -rsbol [value] | The full path of external SBOL files to be referenced in the SBML2SBOL conversion |
| -s [value] | select only this object and those it references |
| -t [value] |  uses types in URIs |
| -v [value] | mark version of data objects created during conversion |

## Example run for SBOL to SBML:
```
  java -jar target/iBioSim-conversion-3.0.0-SNAPSHOT-jar-with-dependencies.jar "-l", "SBML", "-esf", "-p", "http://www.async.ece.utah.edu/", "-oDir", "src/test/resources/edu/utah/ece/async/ibiosim/conversion/SBML_Files/", "-o", "CRISPR_example_out2", "src/test/resources/edu/utah/ece/async/ibiosim/conversion/SBOL_Files/CRISPR_example.xml"
```

## Example run for SBML to SBOL:
```
  java -jar target/iBioSim-conversion-3.0.0-SNAPSHOT-jar-with-dependencies.jar "-l", "SBOL2", "-p", "http://www.async.ece.utah.edu/", "-o", "src/test/resources/edu/utah/ece/async/ibiosim/conversion/SBOL_Files/INV0_output", "src/test/resources/edu/utah/ece/async/ibiosim/conversion/SBML_Files/INV0.xml"
```

## Pre-installation Requirement(s)
* [libSBOLj](https://github.com/SynBioDex/libSBOLj)
* VPR API jar provided from Newcastle Unveristy folks

## Building Converter.java:
1. Open up your command line  
    1. Navigate to the libSBOLj project and perform ```mvn install```
    2. Navigate to conversion package in iBioSim  
        1. type ```mvn clean``` to bring in dependencies to iBioSim conversion package
        2. type ```mvn package``` to build the JAR 

## Replacing VPR API jar in iBioSim:
1. Go to the [lib](https://github.com/MyersResearchGroup/iBioSim/tree/master/lib) folder of the iBioSim directory and delete the old VPR API jar. There should be 3 VPR API jars that should be deleted. For example:
    1. vpr-rdf-util-2.0.4.jar
    2. vpr-sbol-util-2.0.4.jar
    3. vpr.data-2.0.4.jar
2. Add in the 3 new VPR API jars
3. Modify the dependencies in the conversion's pom.xml file to use the new version of the VPR API jars.

## Building VPR API for iBioSim GUI:
1. Open up your command line 
2. Navigate to conversion package in iBioSim
    1. type ```mvn clean``` to bring in dependencies to iBioSim conversion package
    2. type ```mvn install``` to build the JAR 
    3. type ```mvn update``` to update iBioSim workspace
    

