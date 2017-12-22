The conversion package currently supports three types of data conversion and VPR API model generation. 

1. Design to modeling: [SBOL2SBML](http://pubs.acs.org/doi/10.1021/sb5003289)  
2. Modeling to Design: [SBML2SBOL](http://pubs.acs.org/doi/abs/10.1021/acssynbio.5b00212)  
3. Interconversion supported by the libSBOLj2 library: [SBOL1 to SBOL2](http://ieeexplore.ieee.org/document/7440806/)

SBOL2SBML and SBML2SBOL can be build as a standalone JAR. 
There is also a main Java [Converter.java](https://github.com/MyersResearchGroup/iBioSim/blob/master/conversion/src/main/java/edu/utah/ece/async/ibiosim/conversion/Converter.java) file that supports all three types of conversion.

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
