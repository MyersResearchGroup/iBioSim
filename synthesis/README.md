<p align="center">
  <img  src="../docs/media/iBioSim_horizontal.png">
</p>

The synthesis package currently performs technology mapping on combinational designs. 

## How to use it:

After building the executable jar, you need to invoke the following command:

```
  java -jar target/iBioSim-synthesis-3.0.0-SNAPSHOT-jar-with-dependencies.jar [options] input
```

where 

| Required        |  Description  |
| -------------   | ------------- |
| -lf <value>    | full path to the library file containing set of genetic gates needed for technology mapping. |
| -sf <value>    | full path to the specification file to performing technology mapping. |
| -sbml     | perform SBML Technology Mapping |
| -sbol    | perform SBOL Technology Mapping |
| -o  <value>  | name of output file along with full path of where output file will be written to. |
| -osbml  <value>   |  produce solution for technology mapping in SBML format.|
| -osbol  <value>  | produce solution for technology mapping in SBOL format. |

| Options        |  Description  |
| -------------   | ------------- |
| -p [value] | SBOL URI prefix needed to set the SBOLDocument when converting the technology mapping solution to the desired SBOL or SBML data format. |
| -ld [value] |  directory to multiple SBOL or SBML library files |
| -dot [value] | produced SBOL technology mapping solution in dot format. |

## Example run for SBML Technology Mapping:
```
  java -jar target/iBioSim-conversion-3.0.0-SNAPSHOT-jar-with-dependencies.jar "-lf", "", "-sf", "", "-sbml", "-osbml", "-o", "src/test/resources/edu/utah/ece/async/ibiosim/synthesis/SBML_Files/sbml_techmap_sol"
```

## Example run for SBOL Technology Mapping:
```
  java -jar target/iBioSim-conversion-3.0.0-SNAPSHOT-jar-with-dependencies.jar 
```