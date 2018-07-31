
<p align="center">
  <img  src="docs/media/iBioSim_horizontal.png">
</p>

iBioSim is a computer-aided design (CAD) tool aimed for the modeling, analysis, and design of genetic circuits. 
While iBioSim primarily targets models of genetic circuits, models representing metabolic networks, cell-signaling pathways, 
and other biological and chemical systems can also be analyzed. 

iBioSim also includes modeling and visualization support for multi-cellular and spatial models as well. 

It is capable of importing and exporting models specified using the Systems Biology Markup Language (SBML). 
It can import all levels and versions of SBML and is able to export Level 3 Version 1. 
It supports all core SBML modeling constructs except some types of fast reactions, and also has support for the 
hierarchical model composition, layout, flux balance constraints, and arrays packages. 

It has also been tested successfully on the stochastic benchmark suite and the curated models in the BioModels database. 
iBioSim also supports the Synthetic Biology Open Language (SBOL), an emerging standard for information exchange in synthetic 
biology.

##### Website: [iBioSim](http://www.async.ece.utah.edu/ibiosim)
##### Video Demo: [Tools Workflow](https://www.youtube.com/watch?v=g4xayzlyC2Q)
##### Contact: Chris Myers (@cjmyers) myers@ece.utah.edu

Contributor(s): Nathan Barker, Scott Glass, Kevin Jones, Hiroyuki Kuwahara, Curtis Madsen, Nam Nguyen, Tramy Nguyen, Tyler Patterson, Nicholas Roehner, Jason Stevens, Leandro Watanabe, Michael Zhang, Zhen Zhang, and Zach Zundel.

Active Developer(s): Chris Myers, Tramy Nguyen, Leandro Watanabe, Michael Zhang.

## Running iBioSim
1. Download the iBioSim tool from the release page [here](https://github.com/MyersResearchGroup/iBioSim/releases): 
2. After downloading the tool, run the corresponding start-up script:
      * Windows: ```iBioSim.bat``` 
      * Mac OS X: ```iBioSim.mac64```
      * Linux: ```iBioSim.linux64```


## [Optional] Installing iBioSim for Development

### Pre-installation Requirements
1. [Create](https://github.com/) a GitHub account.
2. [Setup](https://help.github.com/articles/set-up-git) Git on your machine.
3. [Install](https://maven.apache.org/download.cgi) Maven plugin on your machine.
4. [Install](http://www.eclipse.org) Eclipse IDE  for Java. 
5. [Install](https://sourceforge.net/projects/sbml/files/libsbml/5.16.0/experimental/) libSBML for validation and flattening.
6. [Clone](https://help.github.com/articles/cloning-a-repository/) the iBioSim GitHub repository to your machine


### Importing iBioSim to Eclipse
1. Clone the iBioSim (https://github.com/MyersResearchGroup/iBioSim.git) project (e.g. ```git clone https://github.com/MyersResearchGroup/iBioSim.git```) to a location of your preference.
2. Open up your Eclipse workspace that you want to import your iBioSim project to.
3. Select Import from the File Menu.
4. When given the option to select which project import, select ```Existing Maven Projects``` under Maven
   * Set Maven Projects:
      * Root Directory: full path to your iBioSim project (i.e. path/to/iBioSim)
      * Once root directory is set, all the pom.xml should be displayed under Projects. Select all pom.xml files.
      * All installation should be complete so click ```Finish```

### Setting up iBioSim Configurations in Eclipse
1. Open up iBioSim ```Run Configurations``` window and create a new ```Java Application``` in your Eclipse workspace
  * Give the java application a name (i.e. iBioSim_GUI)
  * Set the Main tab to the following information:
    * Project: ```iBioSim-gui```
    * Main class: ```edu.utah.ece.async.ibiosim.gui.Gui```
  * Set the Environment tab to the following information:
    * Create variables with the corresponding value:
      * BIOSIM: full path to your iBioSim project (i.e. path/to/iBioSim)
      * PATH: append your copy of iBioSim bin directory to whatever existing PATH already supplied to the value of this variable (i.e. $PATH:path/to/iBioSim/bin).
  * Set Arguments tab to the following information:
    * Program arguments: ```-Xms2048 -Xms2048 -XX:+UseSerialGC -Djava.library.path=/path/to/lib/```
    * Note: for the java library path, ```/path/to/lib/``` is the location where [libSBML is installed](#pre-installation-requirements). The libSBML is installed by default in ```/usr/local/lib``` in Linux and Mac OS X machines and ```C:\Program Files\SBML\libSBML-5.16.0-libxml2-x64``` in Windows 64-bit machines. 
  * If you are running on Mac OS X, also set the following:
    * VM arguments: ```-Dapple.laf.useScreenMenuBar=true -Xdock:name="iBioSim" -Xdock:icon=$BIOSIM/src/resources/icons/iBioSim.jpg```
  * All run configurations are complete. Make sure to apply all your changes.

### [Optional] Building reb2sac and GeneNet dependencies
1. iBioSim incorporates tools that are not Java-based, and therefore, have to be installed separately. 
2. The easiest way to install reb2sac and GeneNet is to simply download the pre-compiled binaries for your operating system below: 
   * [reb2sac](https://github.com/MyersResearchGroup/reb2sac/releases)
   * [GeneNet](https://github.com/MyersResearchGroup/GeneNet/releases)
3. Another way to install them is to compile these tools on your machine following the instructions below:
   * [reb2sac](https://github.com/MyersResearchGroup/reb2sac/)
   * [GeneNet](https://github.com/MyersResearchGroup/GeneNet/)
4. After compiling or downloading reb2sac and GeneNet, copy the compiled binaries into the bin directory in the local copy of your iBioSim.

### Building iBioSim
1. Go to the directory where the iBioSim is checked out and perform ```mvn clean install```. This will install the local non-Maven dependencies into your local repository and then build iBioSim. (NOTE: if you do not want to generate javadocs, use the flag ```-Dmaven.javadoc.skip=true```)
   
