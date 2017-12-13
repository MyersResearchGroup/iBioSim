
<p align="center">
  <img  src="docs/media/iBioSim_horizontal.png">
</p>

# iBioSim Learn

The learn project includes parameter estimation and a regulatory network learning method from time-series data based on the GeneNet procedure.

## How to use it:

After building the executable jar, you need to invoke the following command:

```
  java -jar target/iBioSim-learn-3.0.0-SNAPSHOT-jar-with-dependencies.jar [-e -l] <input file> <directory>
```

where 

* <input file> is an arbitrary SBML file
* <directory> is the location of the time-series data
* -e is a flag to perform parameter estimation. Default is GeneNet. 
* -l is a flag to specify which parameters need to be estimated.


