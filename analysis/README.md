
<p align="center">
  <img  src="../docs/media/iBioSim_horizontal.png">
</p>

# iBioSim Analysis

The analysis project encapsulates the different java-based simulation and 
verification methods that is used in the iBioSim tool. 

## How to use it:

After building the executable jar, you need to invoke the following command:

```
  java -jar target/iBioSim-analysis-3.0.0-SNAPSHOT-jar-with-dependencies.jar [options] input
```

where 

| Required        |  Description  |
| -------------   | ------------- |
| input     | arbitrary SBML, SED-ML, or Combine Archive file |

| Options        |  Description  |
| -------------   | ------------- |
| -d [value] | project directory | 
| -ti [value] | non-negative double initial simulation time | 
| -tl [value] | non-negative double simulation time limit | 
| -ot [value] | non-negative double output time |
| -pi [value] | positive double for print interval |
| -m0 [value] | positive double for minimum step time |
| -m1 [value] | positive double for maximum step time |
| -aErr [value] | positive double for absolute error |
| -sErr [value] | positive double for relative error |
| -sd [value] | long for random seed |
| -r [value] | positive integer for number of runs |
| -sim [value] | simulation type. Options are: ode, hode, ssa, hssa, dfba, jode, jssa. |


