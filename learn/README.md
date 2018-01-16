
<p align="center">
  <img  src="../docs/media/iBioSim_horizontal.png">
</p>

# iBioSim Learn

The learn project includes parameter estimation and a regulatory network learning method from time-series data based on the GeneNet procedure.

## How to use it:

After building the executable jar, you need to invoke the following command:

```
 java -jar target/iBioSim-learn-3.0.0-SNAPSHOT-jar-with-dependencies.jar [options] input directory
```

where 


| Required        |  Description  |
| -------------   | ------------- |
| input     | arbitrary SBML file 
| directory | location of the time-series data  |

| Options        |  Description  |
| -------------   | ------------- |
|  -e  | perform parameter estimation. Default is GeneNet. |
| -l [list] | a flag to specify which parameters need to be estimated, where the value is a comma separated list of parameter ids (e.g., "p1,p2,p3") |
| -ta [num] |  Sets the activation threshold.  Default 1.15| 
| -tr [num] |  Sets the repression threshold.  Default 0.75| 
| -ti [num] |  Sets how high a score must be to be considered a parent.  Default 0.5| 
| -tm [num] |  Sets how close IVs must be in score to be considered for combination.  Default 0.01| 
| -tn [num] |  Sets minimum number of parents to allow through in SelectInitialParents. Default 2| 
| -tj [num] |  Sets the max parents of merged influence vectors, Default 2| 
| -tt [num] |  Sets how fast the bound is relaxed for ta and tr, Default 0.025| 
| -d [num] |   Sets the debug or output level.  Default 0| 
| -wr [num] |  Sets how much larger a number must be to be considered as a rise.  Default 1| 
| -ws [num] |  Sets how far the TSD points are when compared.  Default 1| 
| -nb [num] |  Sets how many bins are used in the evaluation.  Default 4| 
| --lvl |    Writes out the suggested levels for every specie| 
| --readLevels |  Reads the levels from level.lvl file for every specie| 
| --cpp_harshenBoundsOnTie |   Determines if harsher bounds are used when parents tie in CPP.| 
| --cpp_cmp_output_donotInvertSortOrder |  Sets the inverted sort order in the 3 places back to normal| 
| --cpp_seedParents  |  Determines if parents should be ranked by score, not tsd order in CPP.| 
| --cmp_score_mustNotWinMajority |   Determines if score should be used when following conditions are not met a &gt; r+n || r &gt; a + n| 
| --score_donotTossSingleRatioParents |    Determines if single ratio parents should be kept| 
| --output_donotTossChangedInfluenceSingleParents |  Determines if parents that change influence should not be tossed| 
| -binNumbers |  Equal spacing per bin| 
| -noSUCC |  to not use successors in calculating probabilities| 
| -PRED |  use preicessors in calculating probabilities| 
| -basicFBP |  to use the basic FindBaseProb function| 

