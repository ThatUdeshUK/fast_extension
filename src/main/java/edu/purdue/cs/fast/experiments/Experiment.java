package edu.purdue.cs.fast.experiments;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.helper.ObjectSizeCalculator;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;
//import org.openjdk.jol.info.GraphLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Experiment<T> {
    protected String name;
    protected String outputPath;
    protected SpatialKeywordIndex index;
    protected List<Query> queries;
    protected List<Query> preQueries;
    protected List<DataObject> objects;
    public List<DataObject> preObjects;
    protected long creationTime;
    protected long searchTime;
    protected long createMem;
    protected long searchMem;
    protected boolean saveStats = true;
    protected boolean saveTimeline = false;
    protected boolean saveOutput = false;
    protected int seed = 7;
    protected ArrayList<Collection<Query>> results;
    protected ArrayList<Long> searchTimeline = new ArrayList<>();

    abstract void init();

    protected abstract void generateQueries(List<T> list);

    protected abstract void generateObjects(List<T> list);

    protected abstract Metadata generateMetadata();

    abstract void run();

    public void preloadObjects() {
        if (preObjects == null)
            return;

        for (DataObject o : preObjects) {
            index.preloadObject(o);
        }
    }

    public void preloadQueries() {
        if (preQueries == null)
            return;

        for (Query q : preQueries) {
            index.preloadQuery(q);
        }
    }

    public void create() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Query q : queries) {
            index.insertQuery(q);
        }
        stopwatch.stop();
        if (System.getProperty("java.version").equals("1.8") && System.getProperty("java.vendor").equals("AdoptOpenJDK")) {
            long queriesSize = ObjectSizeCalculator.getObjectSize(queries);
            Run.logger.debug("Queries size =" + queriesSize / 1024 + " KB");
            long indexMemorySize = ObjectSizeCalculator.getObjectSize(index) - queriesSize;
            Run.logger.debug("Local index size =" + indexMemorySize / 1024 + " KB");
            createMem = indexMemorySize;
        }
//        createMem = GraphLayout.parseInstance(index).totalSize();
        this.creationTime = stopwatch.elapsed(TimeUnit.NANOSECONDS);
    }

    public void search() {
        results = new ArrayList<>();

        Stopwatch totalTimeWatch = Stopwatch.createStarted();
        for (DataObject o : objects) {
            Stopwatch searchTimeWatch = null;
            if (saveTimeline)
                searchTimeWatch = Stopwatch.createStarted();
            Collection<Query> res = index.insertObject(o);
            if (saveTimeline) {
                assert searchTimeWatch != null;
                searchTimeWatch.stop();
                searchTimeline.add(totalTimeWatch.elapsed(TimeUnit.NANOSECONDS));
            }
            results.add(res);
        }
        totalTimeWatch.stop();
        if (System.getProperty("java.version").equals("1.8") && System.getProperty("java.vendor").equals("AdoptOpenJDK")) {
            long queriesSize = ObjectSizeCalculator.getObjectSize(queries);
            Run.logger.debug("Queries size =" + queriesSize / 1024 + " KB");
            long indexMemorySize = ObjectSizeCalculator.getObjectSize(index) - queriesSize;
            Run.logger.debug("Local index size =" + indexMemorySize / 1024 + " KB");
            searchMem = indexMemorySize;
        }
//        searchMem = GraphLayout.parseInstance(index).totalSize();
        this.searchTime = totalTimeWatch.elapsed(TimeUnit.NANOSECONDS);
    }

    public void save() throws InvalidOutputFile {
        if (!this.saveStats)
            return;

        if (outputPath == null) {
            Run.logger.error("No output file specified. Skipping saving!");
            return;
        }

        Metadata meta = generateMetadata();

        File outputFile = new File(outputPath);
        boolean fileExists = outputFile.exists();
        if (!outputFile.isDirectory()) {
            try {
                if (!fileExists && outputFile.createNewFile()) {
                    StringBuilder header = new StringBuilder("name,creation_time,search_time,create_mem,search_mem");
                    for (String k : meta.getKeys()) {
                        header.append(",").append(k);
                    }

                    FileWriter fw = new FileWriter(outputFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(header + "\n");
                    bw.close();
                    fw.close();
                }

                FileWriter fw = new FileWriter(outputFile, true);
                BufferedWriter bw = new BufferedWriter(fw);

                StringBuilder line = new StringBuilder(name + "," + creationTime + "," + searchTime + "," + createMem +
                        "," + searchMem);
                for (String v : meta.getValues()) {
                    line.append(",").append(v);
                }
                bw.write(line + "\n");
                bw.close();
                fw.close();

                if (saveTimeline) {
                    FileWriter timelineFW = new FileWriter(getSuffixedPath("timeline", meta.getKeys(), meta.getValues()));
                    BufferedWriter timelineBW = new BufferedWriter(timelineFW);
                    for (long v : searchTimeline) {
                        timelineBW.write(v + "\n");
                    }
                    timelineBW.close();
                    timelineFW.close();
                }

                if (saveOutput) {
                    FileWriter outputFW = new FileWriter(getSuffixedPath("output", meta.getKeys(), meta.getValues()));
                    BufferedWriter outputBW = new BufferedWriter(outputFW);
                    for (Collection<Query> v : results) {
                        outputBW.write(Arrays.toString(v.stream().map((query) -> query.id).sorted().toArray()) + "\n");
                    }
                    outputBW.close();
                    outputFW.close();
                }
            } catch (IOException e) {
                throw new InvalidOutputFile(outputPath);
            }
        } else {
            throw new InvalidOutputFile(outputPath);
        }
    }

    private String getSuffixedPath(String type, List<String> keys, List<String> values) {
        int numQueriesI = keys.indexOf("num_queries");
        int numObjectsI = keys.indexOf("num_objects");
        String outSuffix = "_" + type + "_" + values.get(numQueriesI) + "_" + values.get(numObjectsI) + ".csv";

        return outputPath.replace(".csv", outSuffix);
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public ArrayList<Collection<Query>> getResults() {
        return results;
    }

    public void setSaveTimeline() {
        this.saveTimeline = true;
    }

    public void setSaveOutput() {
        this.saveOutput = true;
    }

    /**
     * Sets whether to save the run or not. Overwrite the setSaveTimeline and setSaveOutput settings.
     *
     * @param saveStats to save the run or not.
     */
    public void setSaveStats(boolean saveStats) {
        this.saveStats = saveStats;
    }

    public static class Metadata {
        List<String> keys;
        List<String> values;

        public Metadata() {
            this.keys = new ArrayList<>();
            this.values = new ArrayList<>();
        }

        public void add(String key, String value) {
            this.keys.add(key);
            this.values.add(value);
        }

        public List<String> getKeys() {
            return keys;
        }

        public List<String> getValues() {
            return values;
        }
    }

    public enum IndexType {
        FAST,
        CkQST,
        LFAST,
        AdoptCkQST
    }
}

