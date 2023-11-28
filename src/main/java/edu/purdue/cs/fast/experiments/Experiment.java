package edu.purdue.cs.fast.experiments;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Experiment<T> {
    protected String name;
    protected String outputPath;
    protected SpatialKeywordIndex index;
    protected List<Query> queries;
    protected List<DataObject> objects;
    protected long creationTime;
    protected long searchTime;
    protected long createMem;
    protected long searchMem;
    protected boolean saveStats = true;
    protected boolean saveTimeline = false;
    protected boolean saveOutput = false;
    protected int seed = 7;
    protected ArrayList<List<Query>> results;
    protected ArrayList<Long> searchTimeline = new ArrayList<>();

    abstract void init();

    protected abstract void generateQueries(List<T> list);

    protected abstract void generateObjects(List<T> list);

    abstract void run();

    public void create() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Query q : queries) {
            index.addContinuousQuery(q);
        }
        stopwatch.stop();
//        createMem = GraphLayout.parseInstance(fast).totalSize();
        this.creationTime = stopwatch.elapsed(TimeUnit.NANOSECONDS);
    }

    public void search() {
        results = new ArrayList<>();

        Stopwatch totalTimeWatch = Stopwatch.createStarted();
        for (DataObject o : objects) {
            Stopwatch searchTimeWatch = null;
            if (saveTimeline)
                searchTimeWatch = Stopwatch.createStarted();
            List<Query> res = index.searchQueries(o);
            if (saveTimeline) {
                assert searchTimeWatch != null;
                searchTimeWatch.stop();
                searchTimeline.add(totalTimeWatch.elapsed(TimeUnit.NANOSECONDS));
            }
            results.add(res);
        }
        totalTimeWatch.stop();
//        searchMem = GraphLayout.parseInstance(fast).totalSize();
        this.searchTime = totalTimeWatch.elapsed(TimeUnit.NANOSECONDS);
    }

    public void save(List<String> keys, List<String> values) throws InvalidOutputFile {
        if (!this.saveStats)
            return;

        if (outputPath == null) {
            System.out.println("No output file specified. Skipping saving!");
            return;
        }

        File outputFile = new File(outputPath);
        boolean fileExists = outputFile.exists();
        if (!outputFile.isDirectory()) {
            try {
                if (!fileExists && outputFile.createNewFile()) {
                    StringBuilder header = new StringBuilder("name,creation_time,search_time,create_mem,search_mem,gran,max_x,max_y");
                    for (String k : keys) {
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
                        "," + searchMem); // + "," + index.gridGranularity + "," + index.bounds.max.x + "," + index.bounds.max.y
                for (String v : values) {
                    line.append(",").append(v);
                }
                bw.write(line + "\n");
                bw.close();
                fw.close();

                if (saveTimeline) {
                    FileWriter timelineFW = new FileWriter(getSuffixedPath("timeline", keys, values));
                    BufferedWriter timelineBW = new BufferedWriter(timelineFW);
                    for (long v : searchTimeline) {
                        timelineBW.write(v + "\n");
                    }
                    timelineBW.close();
                    timelineFW.close();
                }

                if (saveOutput) {
                    FileWriter outputFW = new FileWriter(getSuffixedPath("output", keys, values));
                    BufferedWriter outputBW = new BufferedWriter(outputFW);
                    for (List<Query> v : results) {
                        outputBW.write(v.stream().map((query) -> query.id).sorted().toList() + "\n");
                    }
                    outputBW.close();
                    outputFW.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    public ArrayList<List<Query>> getResults() {
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
}

