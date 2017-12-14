
<p align="center">
  <img  src="docs/media/iBioSim_horizontal.png">
</p>

# iBioSim Learn

The learn project includes parameter estimation and a regulatory network learning method from time-series data based on the GeneNet procedure.

## How to use it:

After building the executable jar, you need to invoke the following command:

```
  java -jar target/iBioSim-learn-3.0.0-SNAPSHOT-jar-with-dependencies.jar [-e] [-l ...] input directory
```

where 


| Required        |  Description  |
| -------------   | ------------- |
| ```input```     | arbitrary SBML file 
| ```directory``` | location of the time-series data  |

| Optional        |  Description  |
| -------------   | ------------- |
|  ```-e```  | perform parameter estimation. Default is GeneNet. |
```-l ...``` |a flag to specify which parameters need to be estimated, where the value is a comma separated list of parameter ids (e.g., "p1,p2,p3") |


