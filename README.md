# "FAST: Frequency-Aware Indexing for Spatio-Textual Data Streams" Extensions

---

## Directory Overview

- `src/main/java/<package>` contains the extension of FAST to support KNN queries.
- `src/main/java/<package>/baselines` contains the following baselines.
    - `ckqst` - Reproduction of the paper "Continuous k Nearest Neighbor Queries over Large-Scale Spatial–Textual Data
      Streams"
    - `fast` - Original FAST index for continuous MBR queries
    - `naive` - Naive index for continuous KNN queries
    - `naive` - Naive index to continuous MBR queries
    - `quadtree` - Implementation of a base quadtree
- `src/test/java/<package>` contains the tests for FAST extension and baselines.
- `analysis` contains notebooks with different analysis done.
- `data` contains samples of the datasets.

## Running experiments

```bash
cd <project_root>

# Build the project
mvn package -Dmaven.test.skip

# Specify the experiment to run (Run|RunCkQST|RunNaive|etc..)
CLASS="Run" 
DATA_DIR="<data_directory>" # directory should contain a `data` subdirectory with places dataset. Check `Run.java` for details
RESULT_DIR="./results/" # create if not exists

# Run a experiment with dependencies
java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.$CLASS $PROJECT_DIR $RESULT_DIR
```

> Check `sync_run.sh` for details
