# iBioSim
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

## Pre-installation Requirement(s)
1. [Create](https://github.com/) a GitHub account.
2. [Setup](https://help.github.com/articles/set-up-git) Git on your machine.
3. [Install] (https://maven.apache.org/download.cgi) Maven plugin on your machine.
4. [Clone](https://help.github.com/articles/cloning-a-repository/) the iBioSim GitHub repository to your machine.


## Installing iBioSim in Eclipse
1. Import the iBioSim project into your preferred java IDE tool such as eclipse as a project from Git. 
Note: The remaining set of instructions assumes that you are using Eclipse as your IDE tool.
2. Depending on the option you pick to import projects from git, select:
  * Existing local repository if you have already cloned the iBioSim Github repository to your machine.
  * Clone URI to have your IDE tool import the project for you.
    * Complete the Source Git Repository information by setting:
      * URI: ```https://github.com/MyersResearchGroup/iBioSim.git```
      * host: ```github.com```
      * Repository path: ```/MyersResearchGroup/iBioSim.git```
      * User account: your user github and password account
    * Specify the branch you want to import into your Eclipse workspace. In this case, select ```master```
    * When given the option to select which project import, select ```Import existing Eclipse project```
    * All installation should be complete so click ```Finish```
    * If you have import errors, perform Maven update on the iBioSim project.

## Setting up iBioSim Configurations
1. Open up iBioSim ```Run Configurations``` window and create a new ```Java Application``` in your Eclipse workspace
  * Give the java application a name (i.e. iBioSim_GUI)
  * Set the Main tab to the following information:
    * Project: ```iBioSim```
    * Main class: ```frontend.main.Gui```
  * Set the Environment tab to the following information:
    * Create 2 variables with the corresponding value:
      * BIOSIM: full path to your iBioSim project
      * PATH: append your copy of iBioSim bin directory to whatever existing PATH already supplied to the value of this variable.
  * Set Arguments tab to the following information:
    * Program arguments: ```-Xms2048 -Xms2048 -XX:+UseSerialGC```
    If you are running on a MAC, also set the following:
    * VM arguments: ```-Dapple.laf.useScreenMenuBar=true -Xdock:name="iBioSim" -Xdock:icon=$BIOSIM/src/resources/icons/iBioSim.jpg```
  * All run configurations are complete. Make sure to apply all your changes.

## Running iBioSim
1. Run the java application that you have created from the previous step (i.e. iBioSim_GUI)
