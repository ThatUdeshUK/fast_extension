package edu.purdue.cs.fast;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.purdue.cs.fast.baselines.ckqst.AdoptCkQST;
import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.config.CleanMethod;
import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.experiments.Experiment;
import edu.purdue.cs.fast.experiments.PlacesExperiment;
import edu.purdue.cs.fast.experiments.PlacesExpireExperiment;
import edu.purdue.cs.fast.experiments.PlacesHybridExperiment;
import edu.purdue.cs.fast.experiments.PlacesKNNExperiment;
import edu.purdue.cs.fast.experiments.PlacesKNNExpireExperiment;
import edu.purdue.cs.fast.experiments.PlacesKNNObjExpireExperiment;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.Rectangle;
import edu.purdue.cs.fast.parser.Place;

public class Run5 {
    public static Logger logger = LogManager.getLogger(Experiment.class);

    public static void main(String[] args) {
        // String name = "places_o200000_q5000000_scaled";
        String name = "tweets_o200000_q10000000_spatialuni_max_keys3";
        // String ds = Paths.get(args[1], "data/exported/" + name + ".json").toString();
        String ds = Paths.get("/homes/ukumaras/scratch/twitter-data/" + name + ".json").toString();

        ArrayList<Integer> numQueriesList = new ArrayList<>();
        // numQueriesList.add(1000);
    //    numQueriesList.add(100000);
        // numQueriesList.add(500000);
        numQueriesList.add(1000000);
        // numQueriesList.add(2000000);
        // numQueriesList.add(2500000);
        // numQueriesList.add(5000000);
    //    numQueriesList.add(10000000);
//        numQueriesList.add(20000000);

        ArrayList<Integer> defRatioList = new ArrayList<>();
//        defRatioList.add(2);
//        defRatioList.add(5);
//        defRatioList.add(8);
        // defRatioList.add(10);
//        defRatioList.add(25);
//        defRatioList.add(50);
//        defRatioList.add(75);
        // defRatioList.add(100); // change this

        ArrayList<Double> arList = new ArrayList<>();
        // arList.add(50.0);
        // arList.add(10.0);
        // arList.add(7.5);
        // arList.add(5.0);
        // arList.add(2.5);
        // arList.add(0.0);

        int degRatio = 20;

        for (int numQueries : numQueriesList) {
            // for (double arThresh : arList) {
                PlacesExperiment experiment = (PlacesExperiment) new ExperimentBuilder()
                        .indexType(Experiment.IndexType.AdoptCkQST)
                        .workload(Workload.KNN)
                        .cleanMethod(CleanMethod.NO)
                        .addArg("maxRange", 512)
                        .addArg("numObjects", 100000)
                        .addArg("numPreObjects", 100000)
                        // .addArg("knnRatio", 0)
                        .addArg("numQueries", numQueries)
                        .addArg("maxLevel", 9)
                        // .configKNNFAST(true, false, false, false, degRatio, 100, arThresh)
                        // .hasExternFASTObjectIndex(5)
//                    .hasInternFASTObjectIndex()
                        .paths(ds, args[0])
                    // .saveTimeline()
                        .suffix("_spatialuni_maxk3_mem")
//                    .skipStatSave()
                        .build();

                run(experiment);
//            System.out.println(FAST.context.totalQueryInsertionsIncludingReplications);
//            System.out.println(FAST.context.cellInsertions);
//            runWithoutInf(experiment);
////            runWithSnapshots(experiment);
//            exportPlaces(experiment, args[1] + "fast/data/");
                System.out.println("Done!");
            }
        // }
    }

    private static void run(PlacesExperiment experiment) {
        experiment.run();
    }

    private static void runWithoutInf(PlacesExperiment experiment) {
//        FAST fast = (FAST) experiment.getIndex();
        experiment.init();
        experiment.removeInf(FAST.context.bounds, 5, FAST.context.maxLevel);

        System.gc();
        System.gc();
        System.gc();

        Run.logger.info("Preloading objects and queries!");
        experiment.preloadObjects();
        experiment.preloadQueries();

        Run.logger.info("Creating index!");
        experiment.create();
        Run.logger.info("Creation Done! Time");

        Run.logger.info("Searching!");
        experiment.search();
        Run.logger.info("Search Done! Time");

        try {
            experiment.save();
        } catch (InvalidOutputFile e) {
            Run.logger.error(e.getMessage());
        }
    }

    private static void runWithSnapshots(PlacesExperiment experiment) {
        FAST fast = (FAST) experiment.getIndex();
        experiment.init();

        System.gc();
        System.gc();
        System.gc();

        Run.logger.info("Creating index!");
        experiment.preloadObjects();
        experiment.create();
        saveQuerySnapshot(experiment.getName(), fast, 0);

        Run.logger.info("Searching!");
        int snapFrequency = 25000;
        int numPreObj;
        if (experiment.preObjects != null) {
            numPreObj = experiment.preObjects.size();
        } else {
            numPreObj = 0;
        }

        experiment.search((object) -> {
            if (object.id % snapFrequency == 0 && (object.id - numPreObj) / snapFrequency > 0) {
                System.out.println("Saving snapshot!");
                saveQuerySnapshot(experiment.getName(), fast, (object.id - numPreObj) / snapFrequency);
            }
            return true;
        });
        saveQuerySnapshot(experiment.getName(), fast, 4);
    }

    private static void exportPlaces(PlacesExperiment experiment, String outputPath) {
        try {
            experiment.exportPlaces(outputPath);
        } catch (InvalidOutputFile e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveQuerySnapshot(String expName, FAST fast, int timestamp) {
        try {
            boolean onlyTrie = false;
            String outPath = "results/" + expName;
            if (onlyTrie) {
                outPath += "_onlyTrie";
            }

            FileWriter fw = new FileWriter(outPath + "_Snapshot_" + timestamp + ".csv");
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("id,ar,currentLevel,x,y\n");
            fast.allKNNQueries(onlyTrie).forEach(query -> {
                try {
                    bw.write("" + query.id + ',' + query.ar + ',' + query.currentLevel +
                            ',' + query.location.x + ',' + query.location.y + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            bw.close();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum Workload {
        MBR,
        MBR_EXPIRE,
        HYBRID,
        KNN,
        KNN_EXPIRE,

        KNN_OBJ_EXPIRE,
    }

    public static class ExperimentBuilder {
        private final HashMap<String, Object> kwargs;
        private Experiment.IndexType indexType;
        private Workload workload;
        private CleanMethod cleanMethod = CleanMethod.NO;
        private Config fastConfig;
        private String datasetPath;
        private String outputDir;
        private String suffix = "";
        private boolean hasExternFASTObjIndex = false;
        private boolean hasInternFASTObjectIndex = false;
        private boolean saveTimeline = false;
        private boolean saveStats = true;
        private SpatialKeywordIndex<Query, DataObject> index;
        private Experiment<Place> experiment;

        public ExperimentBuilder() {
            this.kwargs = new HashMap<>();
        }

        public ExperimentBuilder addArg(String key, Object value) {
            kwargs.put(key, value);
            return this;
        }

        public ExperimentBuilder indexType(Experiment.IndexType indexType) {
            this.indexType = indexType;
            return this;
        }

        public ExperimentBuilder workload(Workload workload) {
            this.workload = workload;
            return this;
        }

        public ExperimentBuilder cleanMethod(CleanMethod method) {
            this.cleanMethod = method;
            return this;
        }

        public ExperimentBuilder configKNNFAST(boolean incDescend, boolean lazyObjSearch,
                                               boolean objFastSearch, boolean adaptiveDegRatio,
                                               int degRatio, int knnDegRatio, double arThresh) {
            fastConfig = new Config();
            fastConfig.INCREMENTAL_DESCENT = incDescend;
            fastConfig.LAZY_OBJ_SEARCH = lazyObjSearch;
            fastConfig.OBJ_FAST_SEARCH = objFastSearch;
            fastConfig.ADAPTIVE_DEG_RATIO = adaptiveDegRatio;
            fastConfig.DEGRADATION_RATIO = degRatio;
            fastConfig.KNN_DEGRADATION_RATIO = knnDegRatio;
            fastConfig.KNN_DEGRADATION_AR = arThresh;
            return this;
        }

        public ExperimentBuilder hasExternFASTObjectIndex(int leafCapacity) {
            if (!kwargs.containsKey("numPreObjects")) {
                logger.warn("Have a object index but no pre-objects!");
            }

            this.hasExternFASTObjIndex = true;
            this.kwargs.put("leafCapacity", leafCapacity);
            return this;
        }

        public ExperimentBuilder hasInternFASTObjectIndex() {
            if (!kwargs.containsKey("numPreObjects")) {
                logger.warn("Have a object index but no pre-objects!");
            }

            this.hasInternFASTObjectIndex = true;
            return this;
        }

        public ExperimentBuilder saveTimeline() {
            this.saveTimeline = true;
            return this;
        }

        public ExperimentBuilder skipStatSave() {
            this.saveStats = false;
            return this;
        }

        public ExperimentBuilder paths(String datasetPath, String outputDir) {
            this.datasetPath = datasetPath;
            this.outputDir = outputDir;
            return this;
        }

        public ExperimentBuilder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Experiment<Place> build() {
            initIndex();
            initExperiment();
            experiment.setSaveStats(this.saveStats);
            experiment.setSaveTimeline(this.saveTimeline);
            return experiment;
        }

        public SpatialKeywordIndex<Query, DataObject> getIndex() {
            return index;
        }

        public Experiment<Place> getExperiment() {
            return experiment;
        }

        public void initIndex() {
            if (indexType == null) {
                throw new RuntimeException("First set the `indexType` with .indexType(t)");
            }

            int maxRange = (int) kwargs.getOrDefault("maxRange", 512);
            int maxLevel = (int) kwargs.getOrDefault("maxLevel", 9);
            switch (indexType) {
                case FAST: {
                    int fineGridGran = (int) kwargs.getOrDefault("fineGridGran", (int) Math.pow(2, maxLevel));
                    if (fastConfig == null) {
                        fastConfig = new Config();
                        logger.warn("Using the default FAST config.");
                    }
                    fastConfig.INPLACE_OBJECT_INDEX = hasInternFASTObjectIndex;
                    index = new FAST(
                            fastConfig,
                            new Rectangle(
                                    new Point(0.0, 0.0),
                                    new Point(maxRange, maxRange)
                            ),
                            fineGridGran,
                            maxLevel
                    );
                    if (hasExternFASTObjIndex) {
                        int leafCapacity = (int) kwargs.get("leafCapacity");
                        ((FAST) index).setExternalObjectIndex(leafCapacity, 9);
                    }
                    ((FAST) index).setCleaning(cleanMethod);
                    break;
                }
                case CkQST:
                    index = new CkQST(maxRange, maxRange, maxLevel);
                    break;
                case LFAST:
                    throw new RuntimeException("Vanilla FAST is not supported for this workload.");
                case FAST_NAIVE: {
                    int fineGridGran = (int) kwargs.getOrDefault("fineGridGran", 512);
                    index = new NaiveFAST(
                            new Rectangle(
                                    new Point(0.0, 0.0),
                                    new Point(maxRange, maxRange)
                            ),
                            fineGridGran,
                            maxLevel
                    );
                    break;
                }
                case AdoptCkQST:
                    index = new AdoptCkQST(maxRange, maxRange, maxLevel);
                    break;
                default:
                    throw new RuntimeException("Unsupported Index Type!");
            }
        }

        private void initExperiment() {
            if (workload == null) {
                throw new RuntimeException("First set the `workload` with .workload(w)");
            }

            if (index == null) {
                throw new RuntimeException("First initialize the index with .initIndex()");
            }

            int numQueries = (int) kwargs.getOrDefault("numQueries", 1000000);
            int numObjects = (int) kwargs.getOrDefault("numObjects", 1000000);
            int numKeywords = (int) kwargs.getOrDefault("numKeywords", 5);
            int maxRange = (int) kwargs.getOrDefault("maxRange", 512);

            String outputPath = Paths.get(outputDir, getExpName() + ".csv").toString();
            switch (workload) {
                case MBR_EXPIRE: {
                    double srRate = (double) kwargs.getOrDefault("srRate", 0.01);
                    experiment = new PlacesExpireExperiment(outputPath,
                            datasetPath, index, getExpName(),
                            numQueries, numObjects, numKeywords, srRate, maxRange
                    );
                    break;
                }
                case HYBRID: {
                    double srRate = (double) kwargs.getOrDefault("srRate", 0.01);
                    int knnRatio = (int) kwargs.getOrDefault("knnRatio", 5);
                    int k = (int) kwargs.getOrDefault("k", 5);
                    int numPreObjects = (int) kwargs.getOrDefault("numPreObjects", 0);
                    int numPreQueries = (int) kwargs.getOrDefault("numPreQueries", 0);

                    experiment = new PlacesHybridExperiment(outputPath,
                            datasetPath, index, getExpName(), numPreObjects, numPreQueries, numQueries,
                            numObjects, numKeywords, srRate, k, knnRatio, maxRange, Experiment.IndexType.FAST
                    );
                    break;
                }
                case KNN: {
                    int k = (int) kwargs.getOrDefault("k", 5);
                    int numPreObjects = (int) kwargs.getOrDefault("numPreObjects", 0);
                    int numPreQueries = (int) kwargs.getOrDefault("numPreQueries", 0);

                    Experiment.IndexType type = indexType;
                    if (indexType == Experiment.IndexType.FAST_NAIVE)
                        type = Experiment.IndexType.FAST;

                    experiment = new PlacesKNNExperiment(outputPath,
                            datasetPath, index, getExpName(), numPreObjects,
                            numPreQueries, numQueries, numObjects, numKeywords,
                            k, maxRange, type
                    );
                    break;
                }
                case KNN_EXPIRE: {
                    int k = (int) kwargs.getOrDefault("k", 5);

                    Experiment.IndexType type = indexType;
                    if (indexType == Experiment.IndexType.FAST_NAIVE)
                        type = Experiment.IndexType.FAST;

                    experiment = new PlacesKNNExpireExperiment(outputPath,
                            datasetPath, index, getExpName(), numQueries,
                            numObjects, numKeywords, k, maxRange, type
                    );
                    break;
                }
                case KNN_OBJ_EXPIRE: {
                    int k = (int) kwargs.getOrDefault("k", 5);

                    Experiment.IndexType type = indexType;
                    if (indexType == Experiment.IndexType.FAST_NAIVE)
                        type = Experiment.IndexType.FAST;

                    experiment = new PlacesKNNObjExpireExperiment(outputPath,
                            datasetPath, index, getExpName(), numQueries,
                            numObjects, numKeywords, k, maxRange, type
                    );
                    break;
                }
                default:
                    throw new RuntimeException("Unsupported workload!");
            }
        }

        private String getExpName() {
            String s = indexType.name() + "_" + workload.name() + "_" + cleanMethod.name();
            if (hasExternFASTObjIndex) {
                s += "_ObjIndex";
            }
            return s + this.suffix;
        }
    }
}
